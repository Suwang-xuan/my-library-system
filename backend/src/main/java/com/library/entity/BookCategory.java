package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book_category")
public class BookCategory {

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private String categoryName;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
