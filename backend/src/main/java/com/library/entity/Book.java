package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long bookId;

    private String bookNo;
    
    private String isbn;

    private String bookName;

    private String author;

    private Long categoryId;
    
    @TableField(exist = false)
    private String categoryName;

    private String publisher;

    private LocalDate publishTime;
    
    private String coverUrl;

    private Integer stock;

    private Integer borrowCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
