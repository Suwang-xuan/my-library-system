package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reader")
public class Reader {

    @TableId(type = IdType.AUTO)
    private Long readerId;

    private String readerNo;

    private String readerName;

    private Integer readerType;

    private Integer gender;

    private String phone;

    private String email;

    private String password;

    private Integer maxBorrowNum;

    private Integer status;
    
    @TableField(exist = false)
    private Integer borrowedCount;
    
    @TableField(exist = false)
    private Integer totalBorrowed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
