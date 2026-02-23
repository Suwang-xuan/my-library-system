package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Role;
import com.library.entity.Permission;
import com.library.entity.RolePermission;

import java.util.List;
import java.util.Map;

public interface RoleService extends IService<Role> {

    List<Role> getAllRoles();

    Role getRoleById(Long roleId);

    boolean saveRole(Role role);

    boolean updateRole(Role role);

    boolean deleteRole(Long roleId);

    List<Permission> getPermissionsByRoleId(Long roleId);

    boolean assignPermissions(Long roleId, List<Long> permIds);

}
