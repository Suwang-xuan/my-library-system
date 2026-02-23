package com.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 允许代理的域名列表
    private static final String[] ALLOWED_DOMAINS = {
        "https://img3.doubanio.com",
        "https://img.doubanio.com",
        "https://covers.openlibrary.org"
    };
    
    /**
     * 图片代理接口
     * @param url 原始图片URL
     * @return 代理后的图片响应
     */
    @GetMapping("/cover")
    public ResponseEntity<ByteArrayResource> proxyCoverImage(@RequestParam String url) {
        logger.info("收到图片代理请求，URL: {}", url);
        
        try {
            // 验证URL是否允许代理
            if (!isAllowedUrl(url)) {
                logger.warn("禁止代理的URL: {}", url);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            // 创建请求头，模拟浏览器请求
            HttpHeaders requestHeaders = new HttpHeaders();
            // 添加浏览器User-Agent，模拟Chrome浏览器
            requestHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            // 添加Accept-Language
            requestHeaders.set("Accept-Language", "zh-CN,zh;q=0.9");
            // 添加Accept
            requestHeaders.set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            
            // 封装请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
            
            // 发送带请求头的请求
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
            
            // 获取图片数据
            byte[] imageData = responseEntity.getBody();
            if (imageData == null || imageData.length == 0) {
                logger.warn("图片数据为空: {}", url);
                return ResponseEntity.notFound().build();
            }
            
            // 从响应中获取内容类型，如果没有则根据URL推测
            String contentType = responseEntity.getHeaders().getContentType() != null 
                ? responseEntity.getHeaders().getContentType().toString() 
                : getContentType(url);
            
            // 创建响应头，允许跨域
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Collections.singletonList(HttpMethod.GET));
            headers.setAccessControlAllowHeaders(Arrays.asList("Content-Type"));
            
            // 返回图片数据
            ByteArrayResource resource = new ByteArrayResource(imageData);
            logger.info("图片代理成功，URL: {}, 大小: {} bytes", url, imageData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("图片代理失败: {}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 验证URL是否允许代理
     * @param url 原始URL
     * @return 是否允许代理
     */
    private boolean isAllowedUrl(String url) {
        for (String allowedDomain : ALLOWED_DOMAINS) {
            if (url.startsWith(allowedDomain)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据URL获取内容类型
     * @param url 原始URL
     * @return 内容类型
     */
    private String getContentType(String url) {
        if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (url.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (url.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (url.toLowerCase().endsWith(".webp")) {
            return "image/webp";
        } else {
            // 默认返回jpeg
            return "image/jpeg";
        }
    }
}
