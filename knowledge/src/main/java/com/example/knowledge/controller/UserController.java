package com.example.knowledge.controller;

import com.example.knowledge.dto.LoginDTO;
import com.example.knowledge.entity.User;
import com.example.knowledge.service.UserService;
import com.example.knowledge.common.Result;
import com.example.knowledge.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Result<?>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
            if (user != null) {
                // 生成JWT令牌
                String token = jwtUtils.generateToken(loginDTO.getUsername());

                // 准备返回结果
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("token", token);

                return ResponseEntity.ok(Result.success(response));
            } else {
                return ResponseEntity.status(401).body(Result.error(401, "用户名或密码错误"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error(500, "登录异常：" + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Result<?>> register(@Valid @RequestBody User user) {
        try {
            boolean success = userService.register(user);
            if (success) {
                return ResponseEntity.ok(Result.success("注册成功！"));
            } else {
                return ResponseEntity.status(409).body(Result.error(409, "注册失败，用户名已存在"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error(500, "注册异常：" + e.getMessage()));
        }
    }

    @GetMapping("/findById")
    public ResponseEntity<Result<User>> findById(Long id) {
        try {
            User user = userService.findById(id);
            if (user != null) {
                // 隐藏密码
                user.setPassword(null);
                return ResponseEntity.ok(Result.success(user));
            } else {
                return ResponseEntity.ok(Result.error(404, "用户不存在"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Result.success("登出成功"));
    }

    @GetMapping("/info")
    public ResponseEntity<Result<User>> getUserInfo(@RequestParam Long userId) {
        try {
            User user = userService.findById(userId);
            if (user != null) {
                // 隐藏密码
                user.setPassword(null);
                return ResponseEntity.ok(Result.success(user));
            } else {
                return ResponseEntity.ok(Result.error(404, "用户不存在"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public ResponseEntity<Result<?>> updateUser(@Valid @RequestBody User user,
                                                @RequestParam Long currentUserId) {
        try {
            boolean success = userService.updateUser(user, currentUserId);
            if (success) {
                return ResponseEntity.ok(Result.success("用户信息更新成功"));
            } else {
                return ResponseEntity.status(500).body(Result.error(500, "用户信息更新失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Result.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error(500, "更新异常：" + e.getMessage()));
        }
    }

       /**
     * 更新用户头像
     */
    @PutMapping("/avatar")
    public ResponseEntity<Result<?>> updateAvatar(
            @RequestParam Long userId,
            @RequestParam String avatarUrl) {
        try {
            boolean success = userService.updateAvatar(userId, avatarUrl);
            if (success) {
                return ResponseEntity.ok(Result.success("头像更新成功"));
            } else {
                return ResponseEntity.status(500).body(Result.error(500, "头像更新失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error(500, "更新异常：" + e.getMessage()));
        }
    }

    /**
     * 更新用户密码
     */
    @PutMapping("/password")
    public ResponseEntity<Result<?>> updatePassword(
            @RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        try {
            boolean success = userService.updatePassword(userId, oldPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(Result.success("密码更新成功，请重新登录"));
            } else {
                return ResponseEntity.status(500).body(Result.error(500, "密码更新失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Result.error(403, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error(500, "更新异常：" + e.getMessage()));
        }
    }

    // 在 UserController 中添加
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = jwtUtils.validateToken(token);
                String username = jwtUtils.getUsernameFromToken(token);

                Map<String, Object> result = new HashMap<>();
                result.put("valid", isValid);
                result.put("username", username);
                result.put("token", token.substring(0, 20) + "..."); // 部分token用于调试

                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body("No token provided");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token validation error: " + e.getMessage());
        }
    }
    @GetMapping("/test-jwt")
    public ResponseEntity<?> testJwt(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("status", "error");
            result.put("message", "未提供有效的 Authorization header");
            return ResponseEntity.status(401).body(result);
        }

        String token = authHeader.substring(7);
        result.put("token_length", token.length());
        result.put("token_preview", token.substring(0, Math.min(20, token.length())) + "...");

        // 验证 token
        boolean isValid = jwtUtils.validateToken(token);
        result.put("token_valid", isValid);

        if (isValid) {
            String username = jwtUtils.getUsernameFromToken(token);
            result.put("username", username);
            result.put("status", "success");
            result.put("message", "JWT Token 验证成功");
        } else {
            result.put("status", "error");
            result.put("message", "JWT Token 验证失败");
        }

        return ResponseEntity.ok(result);
    }

    // 生成测试 token
    @PostMapping("/generate-test-token")
    public ResponseEntity<?> generateTestToken(@RequestParam String username) {
        String token = jwtUtils.generateToken(username);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("status", "success");

        return ResponseEntity.ok(result);
    }

}