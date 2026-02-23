package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Reader;
import com.library.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader")
public class ReaderController {

    private static final Logger logger = LoggerFactory.getLogger(ReaderController.class);

    @Autowired
    private ReaderService readerService;

    @GetMapping("/list")
    public CommonResult<Map<String, Object>> getReaderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String readerNo,
            @RequestParam(required = false) String readerName,
            @RequestParam(required = false) Integer readerType,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String phone) {

        Map<String, Object> query = new java.util.HashMap<>();
        if (readerNo != null && !readerNo.isEmpty()) query.put("readerNo", readerNo);
        if (readerName != null && !readerName.isEmpty()) query.put("readerName", readerName);
        if (readerType != null) query.put("readerType", readerType);
        if (gender != null) query.put("gender", gender);
        if (status != null) query.put("status", status);
        if (phone != null && !phone.isEmpty()) query.put("phone", phone);

        Map<String, Object> result = readerService.getReaderList(pageNum, pageSize, query);
        return CommonResult.success(result);
    }

    @GetMapping("/{readerId}")
    public CommonResult<Reader> getReaderById(@PathVariable Long readerId) {
        Reader reader = readerService.getReaderById(readerId);
        return CommonResult.success(reader);
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addReader(@RequestBody Reader reader) {
        boolean result = readerService.saveReader(reader);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PostMapping("/register")
    public CommonResult<Reader> registerReader(@RequestBody Reader reader) {
        boolean result = readerService.registerReader(reader);
        return result ? CommonResult.success("注册成功", reader) : CommonResult.error("注册失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateReader(@RequestBody Reader reader) {
        boolean result = readerService.updateReader(reader);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @PutMapping("/status/{readerId}")
    public CommonResult<Boolean> updateReaderStatus(
            @PathVariable Long readerId,
            @RequestParam Integer status) {
        boolean result = readerService.updateStatus(readerId, status);
        return result ? CommonResult.success("状态更新成功", true) : CommonResult.error("状态更新失败");
    }

    @DeleteMapping("/delete/{readerId}")
    public CommonResult<Boolean> deleteReader(@PathVariable Long readerId) {
        boolean result = readerService.deleteReader(readerId);
        return result ? CommonResult.success("注销成功", true) : CommonResult.error("注销失败");
    }

    @DeleteMapping("/batch-delete")
    public CommonResult<Boolean> batchDeleteReaders(@RequestBody List<Long> readerIds) {
        boolean result = readerService.batchDeleteReaders(readerIds);
        return result ? CommonResult.success("批量注销成功", true) : CommonResult.error("批量注销失败");
    }

    @GetMapping("/active")
    public CommonResult<List<Reader>> getActiveReaders() {
        List<Reader> readers = readerService.getActiveReaders();
        return CommonResult.success(readers);
    }

    @GetMapping("/stats")
    public CommonResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalReaders", readerService.getTotalReaderCount());
        stats.put("typeStats", readerService.getReaderTypeStats());
        return CommonResult.success(stats);
    }

}
