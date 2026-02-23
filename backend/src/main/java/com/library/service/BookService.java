package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Book;

import java.util.List;
import java.util.Map;

public interface BookService extends IService<Book> {

    Map<String, Object> getBookList(Integer pageNum, Integer pageSize, Map<String, Object> query);

    Book getBookById(Long bookId);

    boolean saveBook(Book book);

    boolean updateBook(Book book);

    boolean deleteBook(Long bookId);

    boolean batchDeleteBooks(List<Long> bookIds);

    List<Book> getHotBooks(Integer limit);

    List<Book> getLowStockBooks(Integer threshold);

    int getTotalBookCount();

    int getTotalStock();

}
