package com.example.knowledge.service;

import com.example.knowledge.entity.User;

import java.util.List;

public interface UserService {
    public User findById(Long id);

    // 新增：用户登录
    User login(String username, String password);

    // 新增：用户注册
    boolean register(User user);

    // 新增：更新用户信息
    boolean updateUser(User user, Long currentUserId);

    // 新增修改头像方法
    boolean updateAvatar(Long userId, String avatarUrl);

    // 新增修改密码方法
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
