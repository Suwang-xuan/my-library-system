package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    IPage<Book> selectBookPage(Page<Book> page, @Param("query") Map<String, Object> query);

    @Select("SELECT * FROM book WHERE book_id = #{bookId}")
    Book selectByBookId(@Param("bookId") Long bookId);

    @Select("SELECT * FROM book WHERE is_deleted = 0 ORDER BY borrow_count DESC LIMIT #{limit}")
    List<Book> selectHotBooks(@Param("limit") Integer limit);

    @Select("SELECT * FROM book WHERE stock < #{threshold} AND is_deleted = 0")
    List<Book> selectLowStockBooks(@Param("threshold") Integer threshold);

}
