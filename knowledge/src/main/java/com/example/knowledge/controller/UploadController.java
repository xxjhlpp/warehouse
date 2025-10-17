package com.example.knowledge.controller;

import com.example.knowledge.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    // 从配置文件读取上传根路径
    @Value("${upload.base-path}")
    private String basePath;

    // 从配置文件读取访问前缀
    @Value("${upload.access-prefix}")
    private String accessPrefix;

    /**
     * 封面图片上传接口
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证用户权限（实际项目中应通过拦截器统一验证）
            if (!hasUploadPermission(userId, request)) {
                result.put("code", 403);
                result.put("msg", "上传权限不足");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            // 2. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("msg", "请上传图片文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 调用服务层处理上传
            String originalFilename = file.getOriginalFilename();
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "cover/" + userId + "/" + UUID.randomUUID() + fileExt;

            String fileUrl = uploadService.uploadFile(file, basePath, fileName);

            // 4. 构建返回结果
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", Map.of("url", accessPrefix + fileName));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 普通文件上传接口
     */
    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证用户权限
            if (!hasUploadPermission(userId, request)) {
                result.put("code", 403);
                result.put("msg", "上传权限不足");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            // 2. 验证文件大小（50MB）
            if (file.getSize() > 50 * 1024 * 1024) {
                result.put("code", 400);
                result.put("msg", "文件大小不能超过50MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 调用服务层处理上传
            String originalFilename = file.getOriginalFilename();
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "file/" + userId + "/" + UUID.randomUUID() + fileExt;

            String fileUrl = uploadService.uploadFile(file, basePath, fileName);

            // 4. 构建返回结果
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", Map.of("url", accessPrefix + fileName));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 验证用户上传权限
     */
    private boolean hasUploadPermission(Long userId, HttpServletRequest request) {
        // 实际项目中应通过token验证用户身份
        // 这里简化处理，仅验证userId不为空
        return userId != null && userId > 0;
    }
}
