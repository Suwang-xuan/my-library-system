package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookCategoryMapper extends BaseMapper<BookCategory> {

    @Select("SELECT * FROM book_category WHERE category_id = #{categoryId}")
    BookCategory selectByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT * FROM book_category ORDER BY category_id")
    List<BookCategory> selectAllCategories();

}
