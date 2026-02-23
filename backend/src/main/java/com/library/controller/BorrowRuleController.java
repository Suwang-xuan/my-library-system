package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.BorrowRule;
import com.library.service.BorrowRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/borrow/rule")
public class BorrowRuleController {
    
    @Autowired
    private BorrowRuleService borrowRuleService;

    @GetMapping("/list")
    public CommonResult<List<BorrowRule>> getAllRules() {
        List<BorrowRule> rules = borrowRuleService.getAllRules();
        return CommonResult.success(rules);
    }
    
    @GetMapping("/getByReaderType")
    public CommonResult<BorrowRule> getRuleByReaderType(@RequestParam Integer readerType) {
        BorrowRule rule = borrowRuleService.getRuleByReaderType(readerType);
        return CommonResult.success(rule);
    }
    
    @PostMapping("/update")
    public CommonResult<Boolean> updateRule(@RequestBody BorrowRule borrowRule) {
        boolean result = borrowRuleService.updateRule(borrowRule);
        return result ? CommonResult.success(true) : CommonResult.failed("更新借阅规则失败");
    }
    
    @PostMapping("/add")
    public CommonResult<Boolean> addRule(@RequestBody BorrowRule borrowRule) {
        boolean result = borrowRuleService.save(borrowRule);
        return result ? CommonResult.success(true) : CommonResult.failed("添加借阅规则失败");
    }
    
    @DeleteMapping("/delete/{ruleId}")
    public CommonResult<Boolean> deleteRule(@PathVariable Long ruleId) {
        boolean result = borrowRuleService.removeById(ruleId);
        return result ? CommonResult.success(true) : CommonResult.failed("删除借阅规则失败");
    }
}
