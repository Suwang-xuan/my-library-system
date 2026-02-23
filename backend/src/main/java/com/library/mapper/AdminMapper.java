package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    Admin selectByAdminId(@Param("adminId") Long adminId);

    @Select("SELECT * FROM admin WHERE admin_account = #{account}")
    Admin selectByAccount(@Param("account") String account);

    @Select("SELECT * FROM admin WHERE status = 1")
    List<Admin> selectActiveAdmins();

    @Select("SELECT COUNT(*) FROM admin WHERE status = 1")
    int countActiveAdmins();

}
