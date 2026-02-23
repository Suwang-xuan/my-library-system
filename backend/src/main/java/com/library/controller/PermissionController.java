package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Permission;
import com.library.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/list")
    public CommonResult<List<Permission>> getPermissionList() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return CommonResult.success(permissions);
    }

    @GetMapping("/{permId}")
    public CommonResult<Permission> getPermissionById(@PathVariable Long permId) {
        Permission permission = permissionService.getPermissionById(permId);
        return CommonResult.success(permission);
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addPermission(@RequestBody Permission permission) {
        boolean result = permissionService.savePermission(permission);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updatePermission(@RequestBody Permission permission) {
        boolean result = permissionService.updatePermission(permission);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @DeleteMapping("/delete/{permId}")
    public CommonResult<Boolean> deletePermission(@PathVariable Long permId) {
        boolean result = permissionService.deletePermission(permId);
        return result ? CommonResult.success("删除成功", true) : CommonResult.error("删除失败");
    }

}
