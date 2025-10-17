package com.example.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.util.DigestUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;

    private String email;
    private String avatar;
    private String phone;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 密码加密方法
    public void encryptPassword() {
        if (this.password != null) {
            this.password = DigestUtils.md5DigestAsHex(this.password.getBytes());
        }
    }
}