package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.BorrowRequest;
import com.library.dto.RenewRequest;
import com.library.dto.ReturnRequest;
import com.library.entity.BorrowRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BorrowRecordService extends IService<BorrowRecord> {

    Map<String, Object> getBorrowRecordList(Integer pageNum, Integer pageSize, Map<String, Object> query);

    BorrowRecord getRecordById(Long recordId);

    boolean borrowBook(BorrowRequest request);

    boolean returnBook(ReturnRequest request);

    boolean renewBook(RenewRequest request);

    List<BorrowRecord> getReaderBorrowHistory(Long readerId);

    List<BorrowRecord> getOverdueRecords();

    List<BorrowRecord> getDueRecords(LocalDate date);

    int getActiveBorrowCount();

    Map<String, Object> getBorrowStats(LocalDate startDate, LocalDate endDate);

}
