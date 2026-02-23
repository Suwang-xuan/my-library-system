package com.library.service.impl;

import com.library.service.BookCoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BookCoverServiceImpl implements BookCoverService {

    private static final Logger logger = LoggerFactory.getLogger(BookCoverServiceImpl.class);
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // 本地封面图片列表
    private static final String[] LOCAL_COVERS = {
        "/api/file/cover/suo.jpg",
        "/api/file/cover/ya.jpg"
    };
    
    // Redis缓存键前缀
    private static final String COVER_CACHE_PREFIX = "book:cover:";
    
    // 缓存过期时间（7天）
    private static final long CACHE_EXPIRE_DAYS = 7;
    
    /**
     * 根据图书信息获取封面URL
     * @param isbn 图书ISBN
     * @param bookId 图书ID，用于ISBN为空时生成唯一封面
     * @return 封面URL
     */
    public String getBookCoverByIsbnAndId(String isbn, Long bookId) {
        logger.info("根据ISBN和bookId获取封面: ISBN={}, bookId={}", isbn, bookId);
        
        // 生成缓存键
        String cacheKey = generateCacheKey(isbn, bookId);
        
        // 尝试从Redis获取封面URL
        String coverUrl = getCoverFromCache(cacheKey);
        if (coverUrl != null) {
            logger.info("从Redis缓存获取封面URL: {}", coverUrl);
            return coverUrl;
        }
        
        // 如果缓存中没有，生成新的封面URL
        int coverIndex = (int) (Math.abs(bookId) % LOCAL_COVERS.length);
        coverUrl = LOCAL_COVERS[coverIndex];
        
        // 保存到Redis缓存
        saveCoverToCache(cacheKey, coverUrl);
        
        logger.info("生成并缓存封面URL: bookId={}, URL={}", bookId, coverUrl);
        return coverUrl;
    }
    
    @Override
    public String getBookCoverByIsbn(String isbn) {
        logger.warn("不推荐直接使用ISBN获取封面，应使用ISBN和bookId组合");
        // 兼容旧方法，使用默认bookId=0
        return getBookCoverByIsbnAndId(isbn, 0L);
    }
    
    @Override
    public Map<String, String> batchGetBookCoversByIsbn(String[] isbnList) {
        logger.info("批量获取封面，ISBN列表: {}", Arrays.toString(isbnList));
        
        Map<String, String> result = new HashMap<>();
        if (isbnList == null || isbnList.length == 0) {
            return result;
        }
        
        for (String isbn : isbnList) {
            // 使用ISBN的hashCode作为bookId
            long bookId = isbn.hashCode();
            result.put(isbn, getBookCoverByIsbnAndId(isbn, bookId));
        }
        
        return result;
    }
    
    @Override
    public String getCoverFromCache(String cacheKey) {
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            logger.error("从Redis获取封面失败: {}", cacheKey, e);
            return null;
        }
    }
    
    @Override
    public void saveCoverToCache(String cacheKey, String coverUrl) {
        try {
            redisTemplate.opsForValue().set(cacheKey, coverUrl, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            logger.info("封面URL保存到Redis缓存: {}", cacheKey);
        } catch (Exception e) {
            logger.error("封面URL保存到Redis失败: {}", cacheKey, e);
        }
    }
    
    @Override
    public String getDefaultCover() {
        logger.info("获取默认封面URL");
        return LOCAL_COVERS[0];
    }
    
    /**
     * 清理过期的封面缓存
     * 定期调用此方法清理过期缓存，释放Redis内存
     */
    public void cleanExpiredCovers() {
        try {
            logger.info("开始清理过期封面缓存");
            // Redis会自动清理过期键，这里主要是记录日志
            logger.info("过期封面缓存清理完成");
        } catch (Exception e) {
            logger.error("清理过期封面缓存失败", e);
        }
    }
    
    /**
     * 生成缓存键
     * @param isbn 图书ISBN
     * @param bookId 图书ID
     * @return 缓存键
     */
    private String generateCacheKey(String isbn, Long bookId) {
        // 使用ISBN和bookId组合生成唯一缓存键
        return COVER_CACHE_PREFIX + (isbn != null ? isbn : "") + ":" + bookId;
    }
    
}