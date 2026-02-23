package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long permId;

    private String permName;

    private String permUrl;

    private String permDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
