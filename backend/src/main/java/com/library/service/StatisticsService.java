package com.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getDashboardStats();

    List<Map<String, Object>> getCategoryBorrowStats();

    List<Map<String, Object>> getMonthlyBorrowTrend(Integer year);

    List<Map<String, Object>> getHotBooks(Integer limit);

    List<Map<String, Object>> getLowStockBooks(Integer threshold);

    Map<String, Object> getReaderBehaviorStats();

    List<Map<String, Object>> getOverdueAnalysis(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getBorrowRateStats();

}
