package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long bookId;

    private Long readerId;

    private LocalDateTime borrowTime;

    private LocalDateTime dueTime;

    private LocalDateTime returnTime;

    private Integer overdueDays;

    private BigDecimal overdueFee;

    private Long operatorId;

    private Integer status;

    @TableField(exist = false)
    private String bookNo;

    @TableField(exist = false)
    private String bookName;

    @TableField(exist = false)
    private String readerNo;

    @TableField(exist = false)
    private String readerName;

    @TableField(exist = false)
    private String adminName;

    @TableField(exist = false)
    private Integer readerType;

    @TableField(exist = false)
    private String categoryName;

}
