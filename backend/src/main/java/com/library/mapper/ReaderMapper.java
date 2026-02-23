package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Reader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReaderMapper extends BaseMapper<Reader> {

    IPage<Reader> selectReaderPage(Page<Reader> page, @Param("query") Map<String, Object> query);

    @Select("SELECT * FROM reader WHERE reader_id = #{readerId}")
    Reader selectByReaderId(@Param("readerId") Long readerId);

    @Select("SELECT * FROM reader WHERE reader_no = #{readerNo}")
    Reader selectByReaderNo(@Param("readerNo") String readerNo);

    @Select("SELECT * FROM reader WHERE is_deleted = 0 AND status = 1 ORDER BY create_time DESC")
    List<Reader> selectActiveReaders();

    @Select("SELECT COUNT(*) FROM reader WHERE is_deleted = 0")
    int countTotalReaders();

    @Select("SELECT COUNT(*) FROM reader WHERE is_deleted = 0 AND reader_type = #{readerType}")
    int countReadersByType(@Param("readerType") Integer readerType);

}
