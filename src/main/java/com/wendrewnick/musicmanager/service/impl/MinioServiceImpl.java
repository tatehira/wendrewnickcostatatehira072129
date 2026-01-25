package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

<<<<<<< HEAD
    @Value("${minio.public-url:http://localhost:9000}")
    private String publicUrl;
=======
    @Value("${minio.url}")
    private String minioInternalUrl;

    @Value("${minio.public-url:${minio.url}}")
    private String minioPublicUrl;
>>>>>>> 33008f12b9d8e7303977a274b3e790130ea573e3

    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
<<<<<<< HEAD
        return publicUrl + "/" + bucketName + "/" + objectName;
=======
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(30, TimeUnit.MINUTES)
                            .build());
            return url.replace(minioInternalUrl, minioPublicUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error generating presigned URL", e);
        }
>>>>>>> 33008f12b9d8e7303977a274b3e790130ea573e3
    }
}
