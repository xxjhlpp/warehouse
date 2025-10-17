package com.example.knowledge.mapper;

import com.example.knowledge.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("select * from user where id=#{id}")
    User findById(Long id);

    @Select("select * from user where username=#{username}")
    User findByUsername(String username);

    // 增加邮箱查询，用于后续扩展
    @Select("select * from user where email=#{email}")
    User findByEmail(String email);

    @Insert("insert into user(username, password, email, phone, created_at, updated_at) " +
            "values(#{username}, #{password}, #{email}, #{phone}, #{createdAt}, #{updatedAt})")
    int register(User user);

    // 新增：更新用户信息
    // src/main/java/com/example/knowledge/mapper/UserMapper.java
    @Update("update user set username=#{user.username}, password=#{user.password}, email=#{user.email}, " +
            "phone=#{user.phone}, avatar=#{user.avatar}, updated_at=#{user.updatedAt} where id=#{userId}")
    int update(@Param("user") User user, @Param("userId") Long userId);

    // 新增更新头像方法
    @Update("update user set avatar=#{avatar}, updated_at=#{updatedAt} where id=#{id}")
    int updateAvatar(User user);

    // 新增更新密码方法
    @Update("update user set password=#{password}, updated_at=#{updatedAt} where id=#{id}")
    int updatePassword(User user);
}