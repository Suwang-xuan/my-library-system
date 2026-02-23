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
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final String ADMIN_ID_HEADER = "X-Admin-Id";
    private static final Long SUPER_ADMIN_ROLE = 1L;

    private static final Set<String> PUBLIC_URLS = new HashSet<>();

    static {
        PUBLIC_URLS.add("/api/login");
        PUBLIC_URLS.add("/api/logout");
        PUBLIC_URLS.add("/api/current-user");
        PUBLIC_URLS.add("/api/check-login");
        PUBLIC_URLS.add("/druid");
        PUBLIC_URLS.add("/swagger");
        PUBLIC_URLS.add("/v3/api-docs");
        PUBLIC_URLS.add("/swagger-ui");
        PUBLIC_URLS.add("/swagger-resources");
        PUBLIC_URLS.add("/webjars");
        PUBLIC_URLS.add("/api/proxy");
        PUBLIC_URLS.add("/api/file");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        for (String publicUrl : PUBLIC_URLS) {
            if (requestURI.startsWith(publicUrl)) {
                return true;
            }
        }

        // 从请求头中获取adminId
        String adminId = request.getHeader(ADMIN_ID_HEADER);
        if (adminId == null || adminId.isEmpty()) {
            sendForbiddenResponse(response, "请先登录");
            return false;
        }

        // 简化处理：假设所有登录用户都有基本权限，仅检查是否登录
        logger.debug("权限校验通过: {} - adminId: {}", requestURI, adminId);
        return true;
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        CommonResult<?> result = CommonResult.error(403, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

}
