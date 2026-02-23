package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.BorrowRequest;
import com.library.dto.RenewRequest;
import com.library.dto.ReturnRequest;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowRule;
import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.BookService;
import com.library.service.BorrowRecordService;
import com.library.service.BorrowRuleService;
import com.library.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowRecordServiceImpl.class);

    private static final BigDecimal OVERDUE_FEE_PER_DAY = new BigDecimal("0.50");

    @Autowired
    private BookService bookService;

    @Autowired
    private ReaderService readerService;
    
    @Autowired
    private BorrowRuleService borrowRuleService;

    @Override
    public Map<String, Object> getBorrowRecordList(Integer pageNum, Integer pageSize, Map<String, Object> query) {
        Page<BorrowRecord> page = new Page<>(pageNum, pageSize);
        IPage<BorrowRecord> result = this.baseMapper.selectBorrowRecordPage(page, query);

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return data;
    }

    @Override
    public BorrowRecord getRecordById(Long recordId) {
        BorrowRecord record = this.baseMapper.selectByRecordId(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(BorrowRequest request) {
        Long bookId = request.getBookId();
        Long readerId = request.getReaderId();
        Long operatorId = request.getOperatorId();
        
        // 读者自己借阅时，operatorId可以为null
        if (operatorId == null) {
            operatorId = 0L; // 使用默认值0表示读者自己借阅
        }

        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        if (book.getStock() <= 0) {
            throw new BusinessException(400, "图书库存不足，无法借阅");
        }

        Reader reader = readerService.getReaderById(readerId);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        if (reader.getStatus() == 0) {
            throw new BusinessException(400, "读者已被列入黑名单，无法借阅图书");
        }

        // 获取当前读者类型的借阅规则
        BorrowRule borrowRule = borrowRuleService.getRuleByReaderType(reader.getReaderType());
        if (borrowRule == null) {
            throw new BusinessException(400, "未找到该读者类型的借阅规则");
        }
        
        Integer maxBorrowNum = borrowRule.getMaxBorrowNum();
        if (maxBorrowNum == null || maxBorrowNum <= 0) {
            throw new BusinessException(400, "借阅规则配置错误，最大借阅数量无效");
        }
        
        int currentBorrowCount = this.baseMapper.countActiveBorrowRecordsByReaderId(readerId);
        if (currentBorrowCount >= maxBorrowNum) {
            throw new BusinessException(400, "读者当前借阅数量已达到上限，无法继续借阅");
        }

        LocalDateTime borrowTime = LocalDateTime.now();
        Integer borrowDays = borrowRule.getBorrowDays();
        if (borrowDays == null || borrowDays <= 0) {
            throw new BusinessException(400, "借阅规则配置错误，借阅天数无效");
        }
        LocalDateTime dueTime = borrowTime.plusDays(borrowDays);

        BorrowRecord record = new BorrowRecord();
        record.setBookId(bookId);
        record.setReaderId(readerId);
        record.setBorrowTime(borrowTime);
        record.setDueTime(dueTime);
        record.setOperatorId(operatorId);
        record.setStatus(1);

        book.setStock(book.getStock() - 1);
        book.setBorrowCount(book.getBorrowCount() + 1);
        bookService.updateBook(book);

        boolean result = this.save(record);
        if (result) {
            logger.info("图书借阅成功: 图书ID={}, 读者ID={}", bookId, readerId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(ReturnRequest request) {
        Long recordId = request.getRecordId();
        Long operatorId = request.getOperatorId();

        logger.info("开始处理归还请求: recordId={}, operatorId={}", recordId, operatorId);

        // 读者自己归还时，operatorId可以为null
        if (operatorId == null) {
            operatorId = 0L; // 使用默认值0表示读者自己归还
        }

        // 获取借阅记录
        BorrowRecord record = null;
        try {
            record = this.getRecordById(recordId);
            logger.info("获取借阅记录成功: {}", record);
        } catch (Exception e) {
            logger.error("获取借阅记录失败: {}", e.getMessage(), e);
            throw new BusinessException(400, "获取借阅记录失败: " + e.getMessage());
        }

        if (record.getStatus() != 1) {
            logger.error("归还失败: 该图书未处于借阅状态，当前状态={}", record.getStatus());
            throw new BusinessException(400, "该图书未处于借阅状态，无法归还");
        }

        LocalDateTime returnTime = LocalDateTime.now();
        long overdueDays = ChronoUnit.DAYS.between(record.getDueTime(), returnTime);
        if (overdueDays < 0) {
            overdueDays = 0;
        }

        BigDecimal overdueFee = BigDecimal.ZERO;
        if (overdueDays > 0) {
            overdueFee = OVERDUE_FEE_PER_DAY.multiply(new BigDecimal(overdueDays));
        }

        // 更新借阅记录
        record.setReturnTime(returnTime);
        record.setOverdueDays((int) overdueDays);
        record.setOverdueFee(overdueFee);
        record.setOperatorId(operatorId);
        record.setStatus(2);

        // 获取图书信息
        Book book = null;
        try {
            book = bookService.getById(record.getBookId());
            if (book == null) {
                throw new BusinessException(404, "图书不存在");
            }
            logger.info("获取图书信息成功: {}", book);
        } catch (Exception e) {
            logger.error("获取图书信息失败: {}", e.getMessage(), e);
            throw new BusinessException(400, "获取图书信息失败: " + e.getMessage());
        }

        // 更新图书库存
        book.setStock(book.getStock() + 1);
        boolean bookUpdateResult = false;
        try {
            bookUpdateResult = bookService.updateById(book);
            logger.info("更新图书库存成功: {}", bookUpdateResult);
        } catch (Exception e) {
            logger.error("更新图书库存失败: {}", e.getMessage(), e);
            throw new BusinessException(400, "更新图书库存失败: " + e.getMessage());
        }

        // 更新借阅记录
        boolean recordUpdateResult = false;
        try {
            recordUpdateResult = this.updateById(record);
            logger.info("更新借阅记录成功: {}", recordUpdateResult);
        } catch (Exception e) {
            logger.error("更新借阅记录失败: {}", e.getMessage(), e);
            throw new BusinessException(400, "更新借阅记录失败: " + e.getMessage());
        }

        if (bookUpdateResult && recordUpdateResult) {
            logger.info("图书归还成功: 记录ID={}, 超期天数={}, 超期费用={}", recordId, overdueDays, overdueFee);
            return true;
        } else {
            logger.error("图书归还失败: 图书更新结果={}, 记录更新结果={}", bookUpdateResult, recordUpdateResult);
            throw new BusinessException(400, "归还失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renewBook(RenewRequest request) {
        Long recordId = request.getRecordId();

        BorrowRecord record = this.getRecordById(recordId);
        if (record.getStatus() != 1) {
            throw new BusinessException(400, "该图书未处于借阅状态，无法续借");
        }

        Book book = bookService.getBookById(record.getBookId());
        Reader reader = readerService.getReaderById(record.getReaderId());
        
        // 获取当前读者类型的借阅规则
        BorrowRule borrowRule = borrowRuleService.getRuleByReaderType(reader.getReaderType());
        if (borrowRule == null) {
            throw new BusinessException(400, "未找到该读者类型的借阅规则");
        }
        
        Integer defaultRenewDays = borrowRule.getRenewDays();
        if (defaultRenewDays == null || defaultRenewDays <= 0) {
            defaultRenewDays = 7; // 默认续借7天
        }
        Integer renewDays = request.getRenewDays() != null ? request.getRenewDays() : defaultRenewDays;

        Integer maxBorrowNum = borrowRule.getMaxBorrowNum();
        if (maxBorrowNum == null || maxBorrowNum <= 0) {
            throw new BusinessException(400, "借阅规则配置错误，最大借阅数量无效");
        }
        
        int currentBorrowCount = this.baseMapper.countActiveBorrowRecordsByReaderId(reader.getReaderId());
        if (currentBorrowCount >= maxBorrowNum) {
            throw new BusinessException(400, "读者当前借阅数量已达到上限，无法续借");
        }

        LocalDateTime newDueTime = record.getDueTime().plusDays(renewDays);
        record.setDueTime(newDueTime);
        record.setStatus(1);

        boolean result = this.updateById(record);
        if (result) {
            logger.info("图书续借成功: 记录ID={}, 新应还时间={}", recordId, newDueTime);
        }
        return result;
    }

    @Override
    public List<BorrowRecord> getReaderBorrowHistory(Long readerId) {
        return this.baseMapper.selectByReaderId(readerId);
    }

    @Override
    public List<BorrowRecord> getOverdueRecords() {
        return this.baseMapper.selectOverdueRecords();
    }

    @Override
    public List<BorrowRecord> getDueRecords(LocalDate date) {
        return this.baseMapper.selectDueRecords(date.atStartOfDay());
    }

    @Override
    public int getActiveBorrowCount() {
        return this.baseMapper.countActiveBorrowRecords();
    }

    @Override
    public Map<String, Object> getBorrowStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeCount", this.getActiveBorrowCount());
        stats.put("overdueCount", this.baseMapper.selectOverdueRecords().size());
        return stats;
    }

}
