package com.wendrewnick.musicmanager.service;

import com.wendrewnick.musicmanager.dto.AlbumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AlbumService {
    Page<AlbumDTO> findAll(String title, Pageable pageable);

    AlbumDTO findById(UUID id);

    AlbumDTO create(AlbumDTO albumDTO, MultipartFile image);

    void delete(UUID id);
}
