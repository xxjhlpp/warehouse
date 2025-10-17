package com.example.knowledge.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadFile(MultipartFile file, String basePath, String fileName) throws Exception;
}
