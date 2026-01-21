package com.wendrewnick.musicmanager.service;

import com.wendrewnick.musicmanager.dto.AlbumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AlbumService {
    Page<AlbumDTO> findAll(String title, String artistName, Pageable pageable);

    AlbumDTO findById(UUID id);

    AlbumDTO create(AlbumDTO albumDTO, List<MultipartFile> images);

    void delete(UUID id);
}
