package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    IPage<BorrowRecord> selectBorrowRecordPage(Page<BorrowRecord> page, @Param("query") Map<String, Object> query);

    @Select("SELECT * FROM borrow_record WHERE record_id = #{recordId}")
    BorrowRecord selectByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT * FROM borrow_record WHERE book_id = #{bookId} AND status = 1")
    List<BorrowRecord> selectActiveBorrowRecordsByBookId(@Param("bookId") Long bookId);

    List<BorrowRecord> selectByReaderId(@Param("readerId") Long readerId);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId} AND status = 1")
    int countActiveBorrowRecordsByReaderId(@Param("readerId") Long readerId);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId}")
    int countTotalBorrowRecordsByReaderId(@Param("readerId") Long readerId);

    @Select("SELECT * FROM borrow_record WHERE status = 3")
    List<BorrowRecord> selectOverdueRecords();

    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 1")
    int countActiveBorrowRecords();

    @Select("SELECT COUNT(*) FROM borrow_record WHERE borrow_time BETWEEN #{startTime} AND #{endTime}")
    int countBorrowRecordsByTime(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM borrow_record WHERE status = 1 AND due_time < #{now}")
    List<BorrowRecord> selectDueRecords(@Param("now") LocalDateTime now);

}
