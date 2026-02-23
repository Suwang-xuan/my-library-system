package com.library.controller;

import com.library.common.CommonResult;
import com.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public CommonResult<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return CommonResult.success(stats);
    }

    @GetMapping("/category-borrow")
    public CommonResult<List<Map<String, Object>>> getCategoryBorrowStats() {
        List<Map<String, Object>> stats = statisticsService.getCategoryBorrowStats();
        return CommonResult.success(stats);
    }

    @GetMapping("/monthly-trend")
    public CommonResult<List<Map<String, Object>>> getMonthlyBorrowTrend(
            @RequestParam(required = false) Integer year) {
        List<Map<String, Object>> trend = statisticsService.getMonthlyBorrowTrend(year);
        return CommonResult.success(trend);
    }

    @GetMapping("/hot-books")
    public CommonResult<List<Map<String, Object>>> getHotBooks(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> hotBooks = statisticsService.getHotBooks(limit);
        return CommonResult.success(hotBooks);
    }

    @GetMapping("/low-stock")
    public CommonResult<List<Map<String, Object>>> getLowStockBooks(
            @RequestParam(required = false, defaultValue = "5") Integer threshold) {
        List<Map<String, Object>> lowStockBooks = statisticsService.getLowStockBooks(threshold);
        return CommonResult.success(lowStockBooks);
    }

    @GetMapping("/reader-behavior")
    public CommonResult<Map<String, Object>> getReaderBehaviorStats() {
        Map<String, Object> stats = statisticsService.getReaderBehaviorStats();
        return CommonResult.success(stats);
    }

    @GetMapping("/overdue-analysis")
    public CommonResult<List<Map<String, Object>>> getOverdueAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<Map<String, Object>> analysis = statisticsService.getOverdueAnalysis(startDate, endDate);
        return CommonResult.success(analysis);
    }

    @GetMapping("/borrow-rate")
    public CommonResult<Map<String, Object>> getBorrowRateStats() {
        Map<String, Object> stats = statisticsService.getBorrowRateStats();
        return CommonResult.success(stats);
    }

}
