package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.entity.Album;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.BusinessException;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.AlbumRepository;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import com.wendrewnick.musicmanager.service.AlbumService;
import com.wendrewnick.musicmanager.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

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
        List<Artist> artistList = artistRepository.findAllById(albumDTO.getArtistIds());

        if (artistList.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum artista encontrado com os IDs fornecidos");
        }

        Set<Artist> artists = new HashSet<>(artistList);

        String coverKey = null;
        if (image != null && !image.isEmpty()) {
            if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
                throw new BusinessException("O arquivo deve ser uma imagem válida (PNG, JPG, etc).");
            }
            if (image.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new BusinessException("A imagem não pode exceder 5MB.");
            }
            coverKey = minioService.uploadFile(image);
        }

        Album album = Album.builder()
                .title(albumDTO.getTitle())
                .year(albumDTO.getYear())
                .artists(artists)
                .coverUrl(coverKey)
                .build();

        Album savedAlbum = albumRepository.save(album);
        AlbumDTO dto = toDTO(savedAlbum);

        messagingTemplate.convertAndSend("/topic/albums", dto);

        return dto;
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
                .artistIds(album.getArtists().stream().map(Artist::getId).collect(Collectors.toList()))
                .artistNames(album.getArtists().stream().map(Artist::getName).collect(Collectors.toList()))
                .coverUrl(presignedUrl)
                .build();
    }
}
