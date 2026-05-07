package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Select;
//import org.mapstruct.Mapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    @Select("select * from user where id = #{id}")
    User getById(Long id);

    void insert(User user);
}
