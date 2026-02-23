package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Role;
import com.library.entity.Permission;
import com.library.entity.RolePermission;
import com.library.exception.BusinessException;
import com.library.mapper.PermissionMapper;
import com.library.mapper.RoleMapper;
import com.library.mapper.RolePermissionMapper;
import com.library.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<Role> getAllRoles() {
        return this.baseMapper.selectAllRoles();
    }

    @Override
    public Role getRoleById(Long roleId) {
        Role role = this.baseMapper.selectByRoleId(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(Role role) {
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new BusinessException(400, "角色名称不能为空");
        }

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleName, role.getRoleName());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "角色名称已存在");
        }

        boolean result = this.save(role);
        if (result) {
            logger.info("新增角色: {}", role.getRoleName());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role) {
        if (role.getRoleId() == null) {
            throw new BusinessException(400, "角色ID不能为空");
        }

        Role existingRole = this.getById(role.getRoleId());
        if (existingRole == null) {
            throw new BusinessException(404, "角色不存在");
        }

        if (StringUtils.hasText(role.getRoleName()) && !role.getRoleName().equals(existingRole.getRoleName())) {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Role::getRoleName, role.getRoleName());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(400, "角色名称已存在");
            }
        }

        boolean result = this.updateById(role);
        if (result) {
            logger.info("更新角色: {}", role.getRoleId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        if (roleId == 1) {
            throw new BusinessException(400, "不能删除超级管理员角色");
        }

        this.rolePermissionMapper.deleteByRoleId(roleId);
        boolean result = this.removeById(roleId);
        if (result) {
            logger.info("删除角色: {}", roleId);
        }
        return result;
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return this.permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permIds) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        this.rolePermissionMapper.deleteByRoleId(roleId);

        if (permIds != null && !permIds.isEmpty()) {
            List<RolePermission> rolePermissions = permIds.stream()
                    .map(permId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermId(permId);
                        return rp;
                    })
                    .collect(Collectors.toList());
            this.rolePermissionMapper.insertBatch(rolePermissions);
        }

        logger.info("角色权限分配: 角色ID={}, 权限数量={}", roleId, permIds != null ? permIds.size() : 0);
        return true;
    }

}
