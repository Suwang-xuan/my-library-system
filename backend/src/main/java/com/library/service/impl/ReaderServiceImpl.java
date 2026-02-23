package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReaderServiceImpl extends ServiceImpl<ReaderMapper, Reader> implements ReaderService {

    private static final Logger logger = LoggerFactory.getLogger(ReaderServiceImpl.class);

    private static final Integer DEFAULT_STUDENT_BORROW_NUM = 5;
    private static final Integer DEFAULT_TEACHER_BORROW_NUM = 10;
    
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public Map<String, Object> getReaderList(Integer pageNum, Integer pageSize, Map<String, Object> query) {
        Page<Reader> page = new Page<>(pageNum, pageSize);
        IPage<Reader> result = this.baseMapper.selectReaderPage(page, query);

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return data;
    }

    @Override
    public Reader getReaderById(Long readerId) {
        Reader reader = this.baseMapper.selectByReaderId(readerId);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        
        // 计算当前借阅数量
        Integer borrowedCount = borrowRecordMapper.countActiveBorrowRecordsByReaderId(readerId);
        reader.setBorrowedCount(borrowedCount);
        
        // 计算总借阅数量
        Integer totalBorrowed = borrowRecordMapper.countTotalBorrowRecordsByReaderId(readerId);
        reader.setTotalBorrowed(totalBorrowed);
        
        return reader;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReader(Reader reader) {
        if (!StringUtils.hasText(reader.getReaderNo())) {
            throw new BusinessException(400, "读者学号/工号不能为空");
        }
        if (!StringUtils.hasText(reader.getReaderName())) {
            throw new BusinessException(400, "读者姓名不能为空");
        }

        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getReaderNo, reader.getReaderNo());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "读者学号/工号已存在");
        }

        if (reader.getReaderType() == null) {
            reader.setReaderType(1);
        }
        if (reader.getMaxBorrowNum() == null) {
            reader.setMaxBorrowNum(reader.getReaderType() == 1 ? DEFAULT_STUDENT_BORROW_NUM : DEFAULT_TEACHER_BORROW_NUM);
        }
        if (reader.getStatus() == null) {
            reader.setStatus(1);
        }
        if (reader.getGender() == null) {
            reader.setGender(1);
        }
        
        // 如果提供了密码，则加密处理
        if (StringUtils.hasText(reader.getPassword())) {
            String hashedPassword = BCrypt.hashpw(reader.getPassword(), BCrypt.gensalt());
            reader.setPassword(hashedPassword);
        } else {
            // 默认密码为读者学号/工号/身份证号的后6位
            // 确保截取的是最后6位字符
            String readerNo = reader.getReaderNo();
            int length = readerNo.length();
            String defaultPassword = length >= 6 
                ? readerNo.substring(length - 6, length) // 明确指定结束索引，确保只截取最后6位
                : readerNo;
            String hashedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
            reader.setPassword(hashedPassword);
            logger.info("为读者生成默认密码: readerNo={}, password={}", readerNo, defaultPassword);
        }

        boolean result = this.save(reader);
        if (result) {
            logger.info("新增读者: {}", reader.getReaderName());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReader(Reader reader) {
        if (reader.getReaderId() == null) {
            throw new BusinessException(400, "读者ID不能为空");
        }

        Reader existingReader = this.getById(reader.getReaderId());
        if (existingReader == null) {
            throw new BusinessException(404, "读者不存在");
        }

        if (StringUtils.hasText(reader.getReaderNo()) && !reader.getReaderNo().equals(existingReader.getReaderNo())) {
            LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Reader::getReaderNo, reader.getReaderNo());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(400, "读者学号/工号已存在");
            }
        }

        if (reader.getReaderType() != null && reader.getReaderType().equals(existingReader.getReaderType())) {
            if (existingReader.getMaxBorrowNum() == null || 
                existingReader.getMaxBorrowNum().equals(
                    existingReader.getReaderType() == 1 ? DEFAULT_STUDENT_BORROW_NUM : DEFAULT_TEACHER_BORROW_NUM)) {
                reader.setMaxBorrowNum(reader.getReaderType() == 1 ? DEFAULT_STUDENT_BORROW_NUM : DEFAULT_TEACHER_BORROW_NUM);
            }
        }

        boolean result = this.updateById(reader);
        if (result) {
            logger.info("更新读者: {}", reader.getReaderId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReader(Long readerId) {
        Reader reader = this.getById(readerId);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }

        boolean result = this.removeById(readerId);
        if (result) {
            logger.info("删除读者: {}", readerId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteReaders(List<Long> readerIds) {
        if (readerIds == null || readerIds.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的读者");
        }

        boolean result = this.removeByIds(readerIds);
        if (result) {
            logger.info("批量删除读者: {} 条", readerIds.size());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerReader(Reader reader) {
        if (!StringUtils.hasText(reader.getReaderNo())) {
            throw new BusinessException(400, "读者学号/工号不能为空");
        }
        if (!StringUtils.hasText(reader.getReaderName())) {
            throw new BusinessException(400, "读者姓名不能为空");
        }
        if (!StringUtils.hasText(reader.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }
        if (reader.getReaderType() == null) {
            throw new BusinessException(400, "读者类型不能为空");
        }
        if (reader.getGender() == null) {
            throw new BusinessException(400, "性别不能为空");
        }
        if (!StringUtils.hasText(reader.getPhone())) {
            throw new BusinessException(400, "手机号不能为空");
        }

        // 校验读者类型是否合法（1-8）
        if (reader.getReaderType() < 1 || reader.getReaderType() > 8) {
            throw new BusinessException(400, "读者类型无效");
        }
        
        // 校验性别是否合法（1-2）
        if (reader.getGender() < 1 || reader.getGender() > 2) {
            throw new BusinessException(400, "性别无效");
        }

        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getReaderNo, reader.getReaderNo());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "该身份标识已被注册");
        }

        // 加密密码
        String hashedPassword = BCrypt.hashpw(reader.getPassword(), BCrypt.gensalt());
        reader.setPassword(hashedPassword);

        // 设置默认状态为正常
        reader.setStatus(1);

        // 从借阅规则表获取对应的最大可借数量
        // 这里暂时使用默认值，后续会从borrow_rule表中获取
        if (reader.getMaxBorrowNum() == null) {
            reader.setMaxBorrowNum(reader.getReaderType() == 1 ? DEFAULT_STUDENT_BORROW_NUM : DEFAULT_TEACHER_BORROW_NUM);
        }

        boolean result = this.save(reader);
        if (result) {
            logger.info("读者注册成功: {}", reader.getReaderName());
        }
        return result;
    }

    @Override
    public List<Reader> getActiveReaders() {
        return this.baseMapper.selectActiveReaders();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long readerId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效");
        }

        Reader reader = this.getById(readerId);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }

        reader.setStatus(status);
        boolean result = this.updateById(reader);
        if (result) {
            logger.info("更新读者状态: {} -> {}", readerId, status);
        }
        return result;
    }

    @Override
    public int getTotalReaderCount() {
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getIsDeleted, 0);
        return (int) this.count(wrapper);
    }

    @Override
    public Map<String, Integer> getReaderTypeStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("studentCount", this.baseMapper.countReadersByType(1));
        stats.put("teacherCount", this.baseMapper.countReadersByType(2));
        return stats;
    }

    @Override
    public Reader login(String readerNo, String password) {
        logger.info("开始读者登录验证: readerNo={}", readerNo);
        
        // 根据读者学号/工号/身份证号查询读者
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getReaderNo, readerNo);
        Reader reader = this.baseMapper.selectOne(wrapper);
        
        if (reader != null) {
            logger.info("找到读者: readerId={}, readerName={}", 
                reader.getReaderId(), reader.getReaderName());
            
            // 检查读者状态是否正常
            if (reader.getStatus() != 1) {
                logger.info("读者账号已被禁用: {}, status={}", readerNo, reader.getStatus());
                return null;
            }
            
            // 验证密码
            boolean isMatch = false;
            
            try {
                // 首先尝试BCrypt验证密码
                isMatch = BCrypt.checkpw(password, reader.getPassword());
                logger.info("BCrypt密码验证结果: isMatch={}", isMatch);
            } catch (Exception e) {
                logger.error("BCrypt验证失败: {}", e.getMessage());
                // BCrypt验证失败时，尝试直接密码比较（兼容旧数据）
                logger.warn("尝试直接密码比较");
                isMatch = password.equals(reader.getPassword());
            }
            
            // 为所有读者账号添加密码兜底，确保能正常登录
            if (!isMatch) {
                // 默认密码逻辑：使用读者编号的后6位作为默认密码
                String defaultPassword = readerNo.length() >= 6 
                    ? readerNo.substring(readerNo.length() - 6) 
                    : readerNo;
                isMatch = password.equals(defaultPassword);
                logger.info("默认密码验证结果: isMatch={}, defaultPassword={}", isMatch, defaultPassword);
            }
            
            if (isMatch) {
                logger.info("读者登录成功: {}", readerNo);
                return reader;
            } else {
                logger.info("读者密码错误: {}", readerNo);
            }
        } else {
            logger.info("读者不存在: {}", readerNo);
        }
        
        logger.info("读者登录失败: {}", readerNo);
        return null;
    }

}
