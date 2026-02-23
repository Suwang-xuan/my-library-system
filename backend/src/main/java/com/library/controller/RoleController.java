package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Role;
import com.library.entity.Permission;
import com.library.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public CommonResult<List<Role>> getRoleList() {
        List<Role> roles = roleService.getAllRoles();
        return CommonResult.success(roles);
    }

    @GetMapping("/{roleId}")
    public CommonResult<Role> getRoleById(@PathVariable Long roleId) {
        Role role = roleService.getRoleById(roleId);
        return CommonResult.success(role);
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addRole(@RequestBody Role role) {
        boolean result = roleService.saveRole(role);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateRole(@RequestBody Role role) {
        boolean result = roleService.updateRole(role);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @DeleteMapping("/delete/{roleId}")
    public CommonResult<Boolean> deleteRole(@PathVariable Long roleId) {
        boolean result = roleService.deleteRole(roleId);
        return result ? CommonResult.success("删除成功", true) : CommonResult.error("删除失败");
    }

    @GetMapping("/{roleId}/permissions")
    public CommonResult<List<Permission>> getRolePermissions(@PathVariable Long roleId) {
        List<Permission> permissions = roleService.getPermissionsByRoleId(roleId);
        return CommonResult.success(permissions);
    }

    @PostMapping("/{roleId}/permissions")
    public CommonResult<Boolean> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permIds) {
        boolean result = roleService.assignPermissions(roleId, permIds);
        return result ? CommonResult.success("权限分配成功", true) : CommonResult.error("权限分配失败");
    }

}
