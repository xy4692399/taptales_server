package com.taptales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taptales.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {
    @Update("UPDATE usr SET total_drunk = total_drunk + #{count} WHERE id = #{userId}")
    int increaseDrunkCount(@Param("userId") Long userId, @Param("count") Integer count);
}