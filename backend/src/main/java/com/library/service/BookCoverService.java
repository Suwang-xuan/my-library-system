package com.library.service;

import java.util.Map;

/**
 * 图书封面服务
 */
public interface BookCoverService {
    
    /**
     * 根据ISBN获取图书封面URL
     * @param isbn 图书ISBN
     * @return 图书封面URL
     */
    String getBookCoverByIsbn(String isbn);
    
    /**
     * 批量获取图书封面URL
     * @param isbnList ISBN列表
     * @return 图书封面URL映射
     */
    Map<String, String> batchGetBookCoversByIsbn(String[] isbnList);
    
    /**
     * 从缓存获取封面URL
     * @param isbn 图书ISBN
     * @return 图书封面URL
     */
    String getCoverFromCache(String isbn);
    
    /**
     * 保存封面URL到缓存
     * @param isbn 图书ISBN
     * @param coverUrl 图书封面URL
     */
    void saveCoverToCache(String isbn, String coverUrl);
    
    /**
     * 获取默认封面URL
     * @return 默认封面URL
     */
    String getDefaultCover();
    
    /**
     * 根据ISBN和bookId获取封面URL
     * @param isbn 图书ISBN
     * @param bookId 图书ID
     * @return 封面URL
     */
    String getBookCoverByIsbnAndId(String isbn, Long bookId);
    
    /**
     * 清理过期的封面缓存
     */
    void cleanExpiredCovers();
    
}
