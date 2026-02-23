package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Admin;

import java.util.List;
import java.util.Map;

public interface AdminService extends IService<Admin> {

    Admin getAdminById(Long adminId);

    Admin getAdminByAccount(String account);

    boolean saveAdmin(Admin admin);

    boolean updateAdmin(Admin admin);

    boolean deleteAdmin(Long adminId);

    boolean updateStatus(Long adminId, Integer status);

    boolean updateAvatar(Long adminId, String avatarUrl);

    List<Admin> getActiveAdmins();

    Map<String, Object> login(String account, String password);

    boolean changePassword(Long adminId, String oldPassword, String newPassword);

}
