package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.entity.User;
import com.example.knowledge.mapper.UserMapper;
import com.example.knowledge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceIpml implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceIpml(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        return userMapper.findById(id);
    }

    @Override
    public User login(String username, String password) {
        // 参数验证
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null; // 用户名不存在
        }

        // 密码加密验证
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (encryptedPassword.equals(user.getPassword())) {
            // 登录成功时清除密码信息，避免泄露
            user.setPassword(null);
            return user;
        }
        return null; // 密码错误
    }

    @Override
    public boolean register(User user) {
        // 参数验证
        if (user == null || !StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return false;
        }

        // 密码加密
        user.encryptPassword();

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // 插入数据库
        int rows = userMapper.register(user);
        return rows > 0;
    }

    // 新增的updateUser方法实现
    @Override
    public boolean updateUser(User user, Long userId) {
        // 参数验证
        if (user == null || userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户信息和用户ID不能为空");
        }

        // 检查用户是否存在
        User existingUser = userMapper.findById(userId);
        if (existingUser == null) {
            return false; // 用户不存在
        }

        // 如果需要更新密码，则进行加密处理
        if (StringUtils.hasText(user.getPassword())) {
            String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(encryptedPassword);
        } else {
            // 如果不更新密码，则保留原密码
            user.setPassword(existingUser.getPassword());
        }

        // 设置更新时间
        user.setUpdatedAt(LocalDateTime.now());
        // 设置用户ID，确保更新的是指定用户
        user.setId(userId);

        // 执行更新操作
        int rows = userMapper.update(user,userId);
        return rows > 0;
    }

    // src/main/java/com/example/knowledge/service/serviceimpl/UserServiceIpml.java
    @Override
    public boolean updateAvatar(Long userId, String avatarUrl) {
        // 参数验证
        if (userId == null || userId <= 0 || !StringUtils.hasText(avatarUrl)) {
            throw new IllegalArgumentException("用户ID和头像URL不能为空");
        }

        // 检查用户是否存在
        User existingUser = userMapper.findById(userId);
        if (existingUser == null) {
            return false;
        }

        // 更新头像和时间
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());

        // 执行更新
        int rows = userMapper.updateAvatar(user);
        return rows > 0;
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 参数验证
        if (userId == null || userId <= 0 || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new IllegalArgumentException("用户ID、旧密码和新密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6个字符");
        }

        // 检查用户是否存在
        User existingUser = userMapper.findById(userId);
        if (existingUser == null) {
            return false;
        }

        // 验证旧密码
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!encryptedOldPassword.equals(existingUser.getPassword())) {
            throw new SecurityException("旧密码不正确");
        }

        // 加密新密码并更新
        String encryptedNewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());

        User user = new User();
        user.setId(userId);
        user.setPassword(encryptedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());

        int rows = userMapper.updatePassword(user);
        return rows > 0;
    }
}