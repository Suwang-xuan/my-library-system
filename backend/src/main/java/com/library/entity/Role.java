package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long roleId;

    private String roleName;

    private String roleDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
