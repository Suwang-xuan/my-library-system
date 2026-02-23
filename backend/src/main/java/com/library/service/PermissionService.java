package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    List<Permission> getAllPermissions();

    Permission getPermissionById(Long permId);

    boolean savePermission(Permission permission);

    boolean updatePermission(Permission permission);

    boolean deletePermission(Long permId);

}
