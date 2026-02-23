package com.library.config;

import com.library.service.impl.BookCoverServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Configuration
@EnableScheduling
public class ScheduledTasksConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksConfig.class);

    @Autowired
    private BookCoverServiceImpl bookCoverService;

    /**
     * 每天凌晨2点执行一次，清理过期的封面缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredBookCovers() {
        logger.info("开始执行定期清理过期封面缓存任务");
        try {
            bookCoverService.cleanExpiredCovers();
            logger.info("定期清理过期封面缓存任务执行完成");
        } catch (Exception e) {
            logger.error("定期清理过期封面缓存任务执行失败", e);
        }
    }
}
