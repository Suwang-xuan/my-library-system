package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BorrowRuleMapper extends BaseMapper<BorrowRule> {
    
    BorrowRule selectByReaderType(Integer readerType);
}
