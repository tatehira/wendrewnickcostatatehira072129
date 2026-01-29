package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.exception.StorageException;
import com.wendrewnick.musicmanager.service.StorageService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Profile("docker")
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioInternalUrl;

    @Value("${minio.public-url:${minio.url}}")
    private String minioPublicUrl;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Arquivo não pode ser vazio");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "arquivo";
        }

        String fileName = UUID.randomUUID() + "_" + originalFilename;
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
            throw new StorageException("Erro ao fazer upload do arquivo", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
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
            throw new StorageException("Erro ao gerar URL pré-assinada", e);
        }
    }
}

