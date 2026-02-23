package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Permission;
import com.library.exception.BusinessException;
import com.library.mapper.PermissionMapper;
import com.library.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Override
    public List<Permission> getAllPermissions() {
        return this.baseMapper.selectAllPermissions();
    }

    @Override
    public Permission getPermissionById(Long permId) {
        Permission permission = this.getById(permId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePermission(Permission permission) {
        if (!StringUtils.hasText(permission.getPermName())) {
            throw new BusinessException(400, "权限名称不能为空");
        }

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermName, permission.getPermName());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "权限名称已存在");
        }

        boolean result = this.save(permission);
        if (result) {
            logger.info("新增权限: {}", permission.getPermName());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission) {
        if (permission.getPermId() == null) {
            throw new BusinessException(400, "权限ID不能为空");
        }

        Permission existingPermission = this.getById(permission.getPermId());
        if (existingPermission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        if (StringUtils.hasText(permission.getPermName()) && !permission.getPermName().equals(existingPermission.getPermName())) {
            LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Permission::getPermName, permission.getPermName());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(400, "权限名称已存在");
            }
        }

        boolean result = this.updateById(permission);
        if (result) {
            logger.info("更新权限: {}", permission.getPermId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permId) {
        Permission permission = this.getById(permId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        boolean result = this.removeById(permId);
        if (result) {
            logger.info("删除权限: {}", permId);
        }
        return result;
    }

}
