package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BorrowRule;
import java.util.List;

public interface BorrowRuleService extends IService<BorrowRule> {
    
    BorrowRule getRuleByReaderType(Integer readerType);
    
    List<BorrowRule> getAllRules();
    
    boolean updateRule(BorrowRule borrowRule);
}
