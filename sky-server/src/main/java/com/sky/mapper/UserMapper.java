package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    @Select("select * from user where id = #{id}")
    User getById(Long id);

    void insert(User user);

    @SuppressWarnings("MybatisXMapperMethodInspection")
    @Select("select DATE_FORMAT(create_time, '%Y-%m-%d') as dateKey, count(id) as total from user where create_time >= #{beginTime} and create_time <= #{endTime} group by dateKey order by dateKey")
    List<Map<String, Object>> sumNewUser(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(*) from user where create_time <= #{endTime}")
    Integer getTotalUserCount(LocalDateTime endTime);
}
