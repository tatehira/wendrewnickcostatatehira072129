package com.wendrewnick.musicmanager.service;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ArtistService {
    Page<ArtistDTO> findAll(String name, Pageable pageable);

    ArtistDTO findById(UUID id);

    ArtistDTO create(ArtistDTO artistDTO);

    ArtistDTO update(UUID id, ArtistDTO artistDTO);

    void delete(UUID id);
}
