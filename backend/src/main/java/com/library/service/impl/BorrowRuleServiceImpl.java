package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BorrowRule;
import com.library.mapper.BorrowRuleMapper;
import com.library.service.BorrowRuleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BorrowRuleServiceImpl extends ServiceImpl<BorrowRuleMapper, BorrowRule> implements BorrowRuleService {
    
    @Override
    public BorrowRule getRuleByReaderType(Integer readerType) {
        return baseMapper.selectByReaderType(readerType);
    }
    
    @Override
    public List<BorrowRule> getAllRules() {
        return baseMapper.selectList(null);
    }
    
    @Override
    public boolean updateRule(BorrowRule borrowRule) {
        return this.updateById(borrowRule);
    }
}
