package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Reader;
import com.library.service.AdminService;
import com.library.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String ADMIN_SESSION_KEY = "adminInfo";
    private static final String READER_SESSION_KEY = "readerInfo";

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ReaderService readerService;

    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String account = (String) request.get("account");
        String password = (String) request.get("password");
        String loginType = (String) request.get("loginType"); // admin或reader

        // 确保loginType有默认值
        if (loginType == null) {
            loginType = "admin"; // 默认管理员登录
        }
        
        logger.info("登录请求: account={}, loginType={}", account, loginType);

        HttpSession session = httpRequest.getSession(true);
        
        if ("reader".equals(loginType)) {
            // 读者登录
            Reader reader = readerService.login(account, password);
            if (reader != null) {
                Map<String, Object> loginResult = new HashMap<>();
                loginResult.put("readerId", reader.getReaderId());
                loginResult.put("readerNo", reader.getReaderNo());
                loginResult.put("readerName", reader.getReaderName());
                loginResult.put("readerType", reader.getReaderType());
                loginResult.put("phone", reader.getPhone());
                loginResult.put("email", reader.getEmail());
                loginResult.put("status", reader.getStatus());
                
                session.setAttribute(READER_SESSION_KEY, loginResult);
                session.setMaxInactiveInterval(1800);
                
                logger.info("读者登录成功: {}", account);
                return CommonResult.success("登录成功", loginResult);
            } else {
                logger.info("读者登录失败: {}", account);
                return CommonResult.error(401, "账号或密码错误");
            }
        } else {
            // 管理员登录
            // 检查管理员账号和密码
            // 临时使用固定账号密码，后续可以接入真实的管理员验证
            if ("admin".equals(account) && "123456".equals(password)) {
                Map<String, Object> loginResult = new HashMap<>();
                loginResult.put("adminId", 1L);
                loginResult.put("adminAccount", account);
                loginResult.put("adminName", "超级管理员");
                loginResult.put("roleId", 1L);

                session.setAttribute(ADMIN_SESSION_KEY, loginResult);
                session.setMaxInactiveInterval(1800);

                logger.info("管理员登录成功: {}", account);
                return CommonResult.success("登录成功", loginResult);
            } else {
                logger.info("管理员登录失败: {}", account);
                return CommonResult.error(401, "账号或密码错误");
            }
        }
    }

    @PostMapping("/logout")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        logger.info("登出成功");
        return CommonResult.success("登出成功", true);
    }

    @GetMapping("/current-user")
    public CommonResult<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return CommonResult.error(401, "未登录");
        }

        // 检查是否为管理员
        @SuppressWarnings("unchecked")
        Map<String, Object> adminInfo = (Map<String, Object>) session.getAttribute(ADMIN_SESSION_KEY);
        if (adminInfo != null) {
            return CommonResult.success(adminInfo);
        }
        
        // 检查是否为读者
        @SuppressWarnings("unchecked")
        Map<String, Object> readerInfo = (Map<String, Object>) session.getAttribute(READER_SESSION_KEY);
        if (readerInfo != null) {
            return CommonResult.success(readerInfo);
        }

        return CommonResult.error(401, "登录已过期");
    }

    @GetMapping("/check-login")
    public CommonResult<Map<String, Object>> checkLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        result.put("isLoggedIn", false);

        if (session != null) {
            // 检查是否为管理员
            @SuppressWarnings("unchecked")
            Map<String, Object> adminInfo = (Map<String, Object>) session.getAttribute(ADMIN_SESSION_KEY);
            if (adminInfo != null) {
                result.put("isLoggedIn", true);
                result.put("userType", "admin");
                result.put("userInfo", adminInfo);
                return CommonResult.success(result);
            }
            
            // 检查是否为读者
            @SuppressWarnings("unchecked")
            Map<String, Object> readerInfo = (Map<String, Object>) session.getAttribute(READER_SESSION_KEY);
            if (readerInfo != null) {
                result.put("isLoggedIn", true);
                result.put("userType", "reader");
                result.put("userInfo", readerInfo);
                return CommonResult.success(result);
            }
        }

        return CommonResult.success(result);
    }

}
