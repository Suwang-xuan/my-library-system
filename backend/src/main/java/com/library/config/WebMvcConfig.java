package com.library.config;

import com.library.interceptor.AuthInterceptor;
import com.library.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，允许访问上传的头像文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/login",
                        "/api/logout",
                        "/api/current-user",
                        "/api/check-login",
                        "/api/admin/login",
                        "/api/admin/register",
                        "/api/reader/**",
                        "/api/borrow/rule/**",
                        "/api/book/**",
                        "/api/category/**",
                        "/api/borrow/**",
                        "/api/file/**",
                        "/api/proxy/**"
                );

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/login",
                        "/api/logout",
                        "/api/current-user",
                        "/api/check-login",
                        "/api/admin/login",
                        "/api/admin/register",
                        "/api/reader/**",
                        "/api/borrow/rule/**",
                        "/api/book/**",
                        "/api/category/**",
                        "/api/borrow/**",
                        "/api/file/**",
                        "/api/proxy/**"
                );
    }

}
