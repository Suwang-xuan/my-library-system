package com.library.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    private static final String LOGIN_URL = "/api/admin/login";
    private static final String ADMIN_ID_HEADER = "X-Admin-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 允许所有认证相关端点
        if (requestURI.equals(LOGIN_URL) || 
            requestURI.equals("/api/login") ||
            requestURI.equals("/api/logout") ||
            requestURI.equals("/api/current-user") ||
            requestURI.equals("/api/check-login")) {
            return true;
        }

        // 允许所有读者相关API，无需登录
        if (requestURI.startsWith("/api/reader/")) {
            return true;
        }

        // 允许借阅规则相关API
        if (requestURI.startsWith("/api/borrow/rule/")) {
            return true;
        }

        // 允许图书查询相关API
        if (requestURI.startsWith("/api/book/")) {
            return true;
        }

        // 允许图书分类相关API
        if (requestURI.startsWith("/api/category/")) {
            return true;
        }
        
        // 允许借阅相关API，无需登录（读者自己借阅）
        if (requestURI.startsWith("/api/borrow/")) {
            return true;
        }

        // 从请求头中获取adminId
        String adminId = request.getHeader(ADMIN_ID_HEADER);
        if (adminId == null || adminId.isEmpty()) {
            sendUnauthorizedResponse(response, "请先登录");
            return false;
        }

        logger.debug("用户已登录: {}，adminId: {}", request.getRequestURI(), adminId);
        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        CommonResult<?> result = CommonResult.error(401, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

}
