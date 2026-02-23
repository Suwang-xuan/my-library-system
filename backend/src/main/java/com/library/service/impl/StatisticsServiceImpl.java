package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BookCategory;
import com.library.entity.Reader;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.BookCategoryMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookCategoryMapper bookCategoryMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalBooks = bookMapper.selectCount(new LambdaQueryWrapper<Book>().eq(Book::getIsDeleted, 0));
        int totalStock = bookMapper.selectList(new LambdaQueryWrapper<Book>().eq(Book::getIsDeleted, 0))
                .stream().mapToInt(Book::getStock).sum();
        long totalReaders = readerMapper.selectCount(new LambdaQueryWrapper<Reader>().eq(Reader::getIsDeleted, 0));
        int activeBorrows = borrowRecordMapper.countActiveBorrowRecords();
        int overdueCount = borrowRecordMapper.selectOverdueRecords().size();

        stats.put("totalBooks", totalBooks);
        stats.put("totalStock", totalStock);
        stats.put("totalReaders", totalReaders);
        stats.put("activeBorrows", activeBorrows);
        stats.put("overdueCount", overdueCount);

        List<Book> hotBooks = bookMapper.selectHotBooks(10);
        stats.put("hotBooks", hotBooks);

        List<Book> lowStockBooks = bookMapper.selectLowStockBooks(5);
        stats.put("lowStockBooks", lowStockBooks);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getCategoryBorrowStats() {
        List<BookCategory> categories = bookCategoryMapper.selectAllCategories();
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);

        Map<Long, Long> categoryBorrowCount = new HashMap<>();
        for (BorrowRecord record : allRecords) {
            Book book = bookMapper.selectByBookId(record.getBookId());
            if (book != null && book.getCategoryId() != null) {
                categoryBorrowCount.merge(book.getCategoryId(), 1L, Long::sum);
            }
        }

        return categories.stream().map(category -> {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", category.getCategoryId());
            item.put("categoryName", category.getCategoryName());
            item.put("borrowCount", categoryBorrowCount.getOrDefault(category.getCategoryId(), 0L));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyBorrowTrend(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
            int count = borrowRecordMapper.countBorrowRecordsByTime(
                    monthStart.atStartOfDay(),
                    monthEnd.atTime(LocalTime.MAX)
            );

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("monthName", String.format("%d年%d月", year, month));
            monthData.put("count", count);
            trend.add(monthData);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getHotBooks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        return bookMapper.selectHotBooks(limit).stream().map(book -> {
            Map<String, Object> item = new HashMap<>();
            item.put("bookId", book.getBookId());
            item.put("bookName", book.getBookName());
            item.put("bookNo", book.getBookNo());
            item.put("author", book.getAuthor());
            item.put("borrowCount", book.getBorrowCount());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getLowStockBooks(Integer threshold) {
        if (threshold == null || threshold <= 0) {
            threshold = 5;
        }

        return bookMapper.selectLowStockBooks(threshold).stream().map(book -> {
            Map<String, Object> item = new HashMap<>();
            item.put("bookId", book.getBookId());
            item.put("bookName", book.getBookName());
            item.put("bookNo", book.getBookNo());
            item.put("stock", book.getStock());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getReaderBehaviorStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Reader> readers = readerMapper.selectList(
                new LambdaQueryWrapper<Reader>().eq(Reader::getIsDeleted, 0)
        );

        long studentCount = readers.stream().filter(r -> r.getReaderType() == 1).count();
        long teacherCount = readers.stream().filter(r -> r.getReaderType() == 2).count();

        Map<String, Long> readerTypeBorrow = new HashMap<>();
        readerTypeBorrow.put("student", 0L);
        readerTypeBorrow.put("teacher", 0L);

        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        for (BorrowRecord record : allRecords) {
            Reader reader = readerMapper.selectByReaderId(record.getReaderId());
            if (reader != null) {
                String type = reader.getReaderType() == 1 ? "student" : "teacher";
                readerTypeBorrow.merge(type, 1L, Long::sum);
            }
        }

        double avgStudentBorrow = studentCount > 0 ? (double) readerTypeBorrow.get("student") / studentCount : 0;
        double avgTeacherBorrow = teacherCount > 0 ? (double) readerTypeBorrow.get("teacher") / teacherCount : 0;

        stats.put("totalReaders", readers.size());
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("totalBorrows", allRecords.size());
        stats.put("studentBorrows", readerTypeBorrow.get("student"));
        stats.put("teacherBorrows", readerTypeBorrow.get("teacher"));
        stats.put("avgStudentBorrow", Math.round(avgStudentBorrow * 100) / 100.0);
        stats.put("avgTeacherBorrow", Math.round(avgTeacherBorrow * 100) / 100.0);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getOverdueAnalysis(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getReturnTime, startTime)
                        .le(BorrowRecord::getReturnTime, endTime)
        );

        List<Map<String, Object>> analysis = new ArrayList<>();

        long totalOverdueRecords = records.stream().filter(r -> r.getOverdueDays() > 0).count();
        int totalOverdueDays = records.stream().mapToInt(BorrowRecord::getOverdueDays).sum();
        double totalOverdueFee = records.stream()
                .mapToDouble(r -> r.getOverdueFee() != null ? r.getOverdueFee().doubleValue() : 0)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("type", "summary");
        summary.put("totalRecords", records.size());
        summary.put("overdueRecords", totalOverdueRecords);
        summary.put("overdueRate", records.size() > 0 ? Math.round((double) totalOverdueRecords / records.size() * 10000) / 100.0 : 0);
        summary.put("totalOverdueDays", totalOverdueDays);
        summary.put("totalOverdueFee", Math.round(totalOverdueFee * 100) / 100.0);
        analysis.add(summary);

        Map<Integer, Long> overdueDistribution = records.stream()
                .filter(r -> r.getOverdueDays() > 0)
                .collect(Collectors.groupingBy(BorrowRecord::getOverdueDays, Collectors.counting()));

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("type", "distribution");
        distribution.put("distribution", overdueDistribution);
        analysis.add(distribution);

        return analysis;
    }

    @Override
    public Map<String, Object> getBorrowRateStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Book> books = bookMapper.selectList(
                new LambdaQueryWrapper<Book>().eq(Book::getIsDeleted, 0)
        );

        long totalBooks = books.size();
        long totalBorrowCount = books.stream().mapToLong(Book::getBorrowCount).sum();
        int totalStock = books.stream().mapToInt(Book::getStock).sum();

        double avgBorrowCount = totalBooks > 0 ? (double) totalBorrowCount / totalBooks : 0;

        long neverBorrowed = books.stream().filter(b -> b.getBorrowCount() == 0).count();
        long frequentlyBorrowed = books.stream().filter(b -> b.getBorrowCount() > 10).count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalBorrowCount", totalBorrowCount);
        stats.put("totalStock", totalStock);
        stats.put("avgBorrowCount", Math.round(avgBorrowCount * 100) / 100.0);
        stats.put("neverBorrowed", neverBorrowed);
        stats.put("frequentlyBorrowed", frequentlyBorrowed);
        stats.put("borrowRate", totalBooks > 0 ? Math.round((double) (totalBooks - neverBorrowed) / totalBooks * 10000) / 100.0 : 0);

        return stats;
    }

}
