package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Book;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.service.BookCoverService;
import com.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    
    @Autowired
    private BookCoverService bookCoverService;

    @Override
    public Map<String, Object> getBookList(Integer pageNum, Integer pageSize, Map<String, Object> query) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        IPage<Book> result = this.baseMapper.selectBookPage(page, query);

        // 为每个图书填充封面URL
        List<Book> books = result.getRecords();
        for (Book book : books) {
            fillBookCover(book);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", books);
        return data;
    }

    @Override
    public Book getBookById(Long bookId) {
        Book book = this.baseMapper.selectByBookId(bookId);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        
        // 填充封面URL
        fillBookCover(book);
        
        return book;
    }
    
    /**
     * 填充图书封面URL
     * @param book 图书对象
     */
    private void fillBookCover(Book book) {
        logger.info("填充图书封面URL，当前coverUrl: {}", book.getCoverUrl());
        // 无论是否已有封面URL，都更新为新的本地封面
        String coverUrl = bookCoverService.getBookCoverByIsbnAndId(book.getIsbn(), book.getBookId());
        book.setCoverUrl(coverUrl);
        logger.info("设置封面URL: {}", coverUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBook(Book book) {
        if (!StringUtils.hasText(book.getBookNo())) {
            throw new BusinessException(400, "图书编号不能为空");
        }
        if (!StringUtils.hasText(book.getBookName())) {
            throw new BusinessException(400, "图书名称不能为空");
        }

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getBookNo, book.getBookNo());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "图书编号已存在");
        }

        if (book.getStock() == null) {
            book.setStock(0);
        }
        if (book.getBorrowCount() == null) {
            book.setBorrowCount(0);
        }

        boolean result = this.save(book);
        if (result) {
            logger.info("新增图书: {}", book.getBookName());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBook(Book book) {
        if (book.getBookId() == null) {
            throw new BusinessException(400, "图书ID不能为空");
        }

        Book existingBook = this.getById(book.getBookId());
        if (existingBook == null) {
            throw new BusinessException(404, "图书不存在");
        }

        if (StringUtils.hasText(book.getBookNo()) && !book.getBookNo().equals(existingBook.getBookNo())) {
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Book::getBookNo, book.getBookNo());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(400, "图书编号已存在");
            }
        }

        boolean result = this.updateById(book);
        if (result) {
            logger.info("更新图书: {}", book.getBookId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBook(Long bookId) {
        Book book = this.getById(bookId);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }

        boolean result = this.removeById(bookId);
        if (result) {
            logger.info("删除图书: {}", bookId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteBooks(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的图书");
        }

        boolean result = this.removeByIds(bookIds);
        if (result) {
            logger.info("批量删除图书: {} 条", bookIds.size());
        }
        return result;
    }

    @Override
    public List<Book> getHotBooks(Integer limit) {
        List<Book> books = this.baseMapper.selectHotBooks(limit);
        // 为热门图书填充封面URL
        for (Book book : books) {
            fillBookCover(book);
        }
        return books;
    }
    
    @Override
    public List<Book> getLowStockBooks(Integer threshold) {
        List<Book> books = this.baseMapper.selectLowStockBooks(threshold);
        // 为低库存图书填充封面URL
        for (Book book : books) {
            fillBookCover(book);
        }
        return books;
    }

    @Override
    public int getTotalBookCount() {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsDeleted, 0);
        return (int) this.count(wrapper);
    }

    @Override
    public int getTotalStock() {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsDeleted, 0);
        return this.list(wrapper).stream().mapToInt(Book::getStock).sum();
    }

}
