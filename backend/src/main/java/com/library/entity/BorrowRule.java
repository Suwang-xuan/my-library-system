package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("borrow_rule")
public class BorrowRule {

    @TableId(type = IdType.AUTO)
    private Long ruleId;

    private Integer readerType;

    private Integer maxBorrowNum;

    private Integer borrowDays;

    private Integer maxRenewTimes;

    private Integer renewDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
