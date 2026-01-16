package com.wendrewnick.musicmanager.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String uploadFile(MultipartFile file);

    String getPresignedUrl(String objectName);
}
