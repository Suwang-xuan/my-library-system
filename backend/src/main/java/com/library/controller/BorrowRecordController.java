package com.library.controller;

import com.library.common.CommonResult;
import com.library.dto.BorrowRequest;
import com.library.dto.RenewRequest;
import com.library.dto.ReturnRequest;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowRecordController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowRecordController.class);

    @Autowired
    private BorrowRecordService borrowRecordService;

    @GetMapping("/list")
    public CommonResult<Map<String, Object>> getBorrowRecordList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bookNo,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String readerNo,
            @RequestParam(required = false) String readerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {

        Map<String, Object> query = new java.util.HashMap<>();
        if (bookNo != null && !bookNo.isEmpty()) query.put("bookNo", bookNo);
        if (bookName != null && !bookName.isEmpty()) query.put("bookName", bookName);
        if (readerNo != null && !readerNo.isEmpty()) query.put("readerNo", readerNo);
        if (readerName != null && !readerName.isEmpty()) query.put("readerName", readerName);
        if (status != null) query.put("status", status);
        if (startTime != null) query.put("startTime", startTime.atStartOfDay());
        if (endTime != null) query.put("endTime", endTime.plusDays(1).atStartOfDay());

        Map<String, Object> result = borrowRecordService.getBorrowRecordList(pageNum, pageSize, query);
        return CommonResult.success(result);
    }

    @GetMapping("/{recordId}")
    public CommonResult<BorrowRecord> getRecordById(@PathVariable Long recordId) {
        BorrowRecord record = borrowRecordService.getRecordById(recordId);
        return CommonResult.success(record);
    }

    @PostMapping("/borrow")
    public CommonResult<Boolean> borrowBook(@RequestBody BorrowRequest request) {
        boolean result = borrowRecordService.borrowBook(request);
        return result ? CommonResult.success("借阅成功", true) : CommonResult.error("借阅失败");
    }

    @PostMapping("/return")
    public CommonResult<Boolean> returnBook(@RequestBody ReturnRequest request) {
        boolean result = borrowRecordService.returnBook(request);
        return result ? CommonResult.success("归还成功", true) : CommonResult.error("归还失败");
    }

    @PostMapping("/renew")
    public CommonResult<Boolean> renewBook(@RequestBody RenewRequest request) {
        boolean result = borrowRecordService.renewBook(request);
        return result ? CommonResult.success("续借成功", true) : CommonResult.error("续借失败");
    }

    @GetMapping("/reader/{readerId}/history")
    public CommonResult<List<BorrowRecord>> getReaderBorrowHistory(@PathVariable Long readerId) {
        logger.info("获取读者借阅历史: readerId={}", readerId);
        List<BorrowRecord> history = borrowRecordService.getReaderBorrowHistory(readerId);
        logger.info("获取到借阅记录: {}", history.size());
        return CommonResult.success(history);
    }

    @GetMapping("/overdue")
    public CommonResult<List<BorrowRecord>> getOverdueRecords() {
        List<BorrowRecord> overdueRecords = borrowRecordService.getOverdueRecords();
        return CommonResult.success(overdueRecords);
    }

    @GetMapping("/due")
    public CommonResult<List<BorrowRecord>> getDueRecords(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<BorrowRecord> dueRecords = borrowRecordService.getDueRecords(date);
        return CommonResult.success(dueRecords);
    }

    @GetMapping("/stats")
    public CommonResult<Map<String, Object>> getStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        Map<String, Object> stats = borrowRecordService.getBorrowStats(startDate, endDate);
        return CommonResult.success(stats);
    }

}
