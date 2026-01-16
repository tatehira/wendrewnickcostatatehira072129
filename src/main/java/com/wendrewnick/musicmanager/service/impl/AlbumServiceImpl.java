package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.entity.Album;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.AlbumRepository;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import com.wendrewnick.musicmanager.service.AlbumService;
import com.wendrewnick.musicmanager.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    @Override
    public Page<AlbumDTO> findAll(String title, Pageable pageable) {
        Page<Album> albums;
        if (title != null && !title.isBlank()) {
            albums = albumRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            albums = albumRepository.findAll(pageable);
        }
        return albums.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public AlbumDTO findById(UUID id) {
        return toDTO(getEntityById(id));
    }

    @Transactional
    @Override
    public AlbumDTO create(AlbumDTO albumDTO, MultipartFile image) {
        Artist artist = artistRepository.findById(albumDTO.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado"));

        String coverKey = null;
        if (image != null && !image.isEmpty()) {
            coverKey = minioService.uploadFile(image);
        }

        Album album = Album.builder()
                .title(albumDTO.getTitle())
                .year(albumDTO.getYear())
                .artist(artist)
                .coverUrl(coverKey)
                .build();

        return toDTO(albumRepository.save(album));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        if (!albumRepository.existsById(id)) {
            throw new ResourceNotFoundException("Álbum não encontrado com o ID: " + id);
        }
        albumRepository.deleteById(id);
    }

    private Album getEntityById(UUID id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com o ID: " + id));
    }

    private AlbumDTO toDTO(Album album) {
        String presignedUrl = null;
        if (album.getCoverUrl() != null) {
            presignedUrl = minioService.getPresignedUrl(album.getCoverUrl());
        }

        return AlbumDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .year(album.getYear())
                .artistId(album.getArtist().getId())
                .artistName(album.getArtist().getName())
                .coverUrl(presignedUrl)
                .build();
    }
}
