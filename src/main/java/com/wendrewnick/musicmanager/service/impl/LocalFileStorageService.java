package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.exception.StorageException;
import com.wendrewnick.musicmanager.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("local")
@RequiredArgsConstructor
public class LocalFileStorageService implements StorageService {

    @Value("${storage.local.base-path:./local-storage}")
    private String basePath;

    @Value("${storage.local.public-url:http://localhost:8080/local-storage}")
    private String publicUrl;

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
        Path directory = Paths.get(basePath).toAbsolutePath().normalize();
        Path target = directory.resolve(fileName).normalize();

        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), target);
            return fileName;
        } catch (IOException e) {
            throw new StorageException("Erro ao salvar arquivo localmente", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new StorageException("Nome do arquivo não pode ser vazio");
        }
        return publicUrl + "/" + objectName;
    }
}

