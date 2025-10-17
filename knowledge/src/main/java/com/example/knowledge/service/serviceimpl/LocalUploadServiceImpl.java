package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.service.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalUploadServiceImpl implements UploadService {

    @Override
    public String uploadFile(MultipartFile file, String basePath, String fileName) throws Exception {
        // 创建文件保存目录
        Path filePath = Paths.get(basePath, fileName);
        File parentDir = filePath.getParent().toFile();

        if (!parentDir.exists()) {
            boolean mkdirs = parentDir.mkdirs();
            if (!mkdirs) {
                throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
            }
        }

        // 保存文件
        Files.write(filePath, file.getBytes());

        // 返回文件路径
        return filePath.toAbsolutePath().toString();
    }
}
