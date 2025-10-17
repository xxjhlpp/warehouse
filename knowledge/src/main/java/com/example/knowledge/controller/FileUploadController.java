package com.example.knowledge.controller;

import com.example.knowledge.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {
    @Value("${upload.avatar-path}")
    private String uploadBaseDir;
    private static final String UPLOAD_BASE_DIR = "D:/app-uploads/avatar/";

    @PostMapping("/upload/avatar")
    public Result uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        try {
            // 2. 确保上传目录存在（关键修复）
            File baseDir = new File(UPLOAD_BASE_DIR);
            if (!baseDir.exists()) {
                // 递归创建目录（包括所有父目录）
                boolean created = baseDir.mkdirs();
                if (!created) {
                    return Result.error("创建上传目录失败，请检查服务器权限");
                }
            }

            // 3. 生成安全的文件名（避免路径注入）
            String originalFileName = file.getOriginalFilename();
            String fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
            String safeFileName = userId + "_" + UUID.randomUUID() + fileExt;

            // 4. 拼接完整路径（使用系统兼容的路径分隔符）
            File destFile = new File(baseDir, safeFileName);

            // 5. 保存文件
            file.transferTo(destFile);

            // 6. 返回可访问的URL（根据实际情况调整）
            String avatarUrl = "/avatars/" + safeFileName;
            return Result.success(avatarUrl);

        } catch (IOException e) {
            // 打印详细错误日志，方便排查
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
