package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Reader;

import java.util.List;
import java.util.Map;

public interface ReaderService extends IService<Reader> {

    Map<String, Object> getReaderList(Integer pageNum, Integer pageSize, Map<String, Object> query);

    Reader getReaderById(Long readerId);

    boolean saveReader(Reader reader);

    boolean updateReader(Reader reader);

    boolean deleteReader(Long readerId);

    boolean batchDeleteReaders(List<Long> readerIds);

    boolean registerReader(Reader reader);

    List<Reader> getActiveReaders();

    boolean updateStatus(Long readerId, Integer status);

    int getTotalReaderCount();

    Map<String, Integer> getReaderTypeStats();
    
    /**
     * 读者登录
     * @param readerNo 读者学号/工号/身份证号
     * @param password 密码
     * @return 读者信息，登录失败返回null
     */
    Reader login(String readerNo, String password);

}
