package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Admin;
import com.library.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Value("${file.upload.path:./uploads/avatars}")
    private String uploadPath;

    @GetMapping("/list")
    public CommonResult<List<Admin>> getAdminList() {
        List<Admin> admins = adminService.getActiveAdmins();
        return CommonResult.success(admins);
    }

    @GetMapping("/{adminId}")
    public CommonResult<Admin> getAdminById(@PathVariable Long adminId) {
        Admin admin = adminService.getAdminById(adminId);
        return CommonResult.success(admin);
    }

    @GetMapping("/current-user")
    public CommonResult<Map<String, Object>> getCurrentUser(@RequestHeader(value = "X-Admin-Id", required = false) String adminIdStr) {
        try {
            Long adminId;
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                adminId = 1L;
            } else {
                adminId = Long.parseLong(adminIdStr);
            }
            
            Admin admin = adminService.getAdminById(adminId);
            if (admin == null) {
                return CommonResult.error(401, "用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("adminId", admin.getAdminId());
            result.put("adminAccount", admin.getAdminAccount());
            result.put("adminName", admin.getAdminName());
            result.put("phone", admin.getPhone());
            result.put("roleId", admin.getRoleId());
            result.put("avatar", admin.getAvatar());
            result.put("status", admin.getStatus());

            return CommonResult.success(result);
        } catch (NumberFormatException e) {
            return CommonResult.error(401, "无效的用户ID");
        }
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addAdmin(@RequestBody Admin admin) {
        boolean result = adminService.saveAdmin(admin);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateAdmin(@RequestBody Admin admin) {
        boolean result = adminService.updateAdmin(admin);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @DeleteMapping("/delete/{adminId}")
    public CommonResult<Boolean> deleteAdmin(@PathVariable Long adminId) {
        boolean result = adminService.deleteAdmin(adminId);
        return result ? CommonResult.success("删除成功", true) : CommonResult.error("删除失败");
    }

    @PutMapping("/status/{adminId}")
    public CommonResult<Boolean> updateAdminStatus(
            @PathVariable Long adminId,
            @RequestParam Integer status) {
        boolean result = adminService.updateStatus(adminId, status);
        return result ? CommonResult.success("状态更新成功", true) : CommonResult.error("状态更新失败");
    }

    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String account = request.get("account");
        String password = request.get("password");
        Map<String, Object> result = adminService.login(account, password);
        return CommonResult.success(result);
    }

    @PutMapping("/password")
    public CommonResult<Boolean> changePassword(@RequestBody Map<String, String> request) {
        Long adminId = Long.parseLong(request.get("adminId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        boolean result = adminService.changePassword(adminId, oldPassword, newPassword);
        return result ? CommonResult.success("密码修改成功", true) : CommonResult.error("密码修改失败");
    }

    @PostMapping("/upload-avatar")
    public CommonResult<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "adminId", required = false) Long adminId) {
        
        logger.info("收到头像上传请求, adminId: {}", adminId);
        
        if (file.isEmpty()) {
            logger.warn("上传文件为空");
            return CommonResult.error("请选择要上传的文件");
        }

        try {
            if (adminId == null) {
                adminId = 1L;
            }
            
            // 生成唯一文件名，避免冲突
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String newFileName = "avatar_" + adminId + "_" + System.currentTimeMillis() + ext;
            
            // 确保上传目录存在
            Path uploadDir = Paths.get("uploads/avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("创建上传目录成功: {}", uploadDir.toString());
            }
            
            // 保存文件
            Path targetPath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("文件保存成功: {}", targetPath.toString());
            
            // 构建头像URL
            String avatarUrl = "/api/file/avatar/" + newFileName;
            
            // 更新管理员头像
            adminService.updateAvatar(adminId, avatarUrl);
            
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("fileName", newFileName);
            
            logger.info("管理员 {} 头像上传成功: {}", adminId, avatarUrl);
            return CommonResult.success("头像上传成功", result);
            
        } catch (IOException e) {
            logger.error("头像上传失败", e);
            return CommonResult.error("头像上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("头像上传发生异常", e);
            return CommonResult.error("头像上传失败: " + e.getMessage());
        }
    }

}
