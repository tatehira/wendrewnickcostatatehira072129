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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    @Override
    public Page<AlbumDTO> findAll(String title, String artistName, Boolean soloOrBand, Pageable pageable) {
        Page<Album> albums;
        if (title != null && !title.isBlank()) {
            albums = albumRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (artistName != null && !artistName.isBlank()) {
            albums = albumRepository.findByArtistsNameContainingIgnoreCase(artistName, pageable);
        } else if (soloOrBand != null) {
            albums = albumRepository.findByArtistType(soloOrBand, pageable);
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
    public AlbumDTO create(AlbumDTO albumDTO, List<MultipartFile> images) {
        if (albumDTO == null) {
            throw new BusinessException("Dados do álbum não podem ser nulos");
        }
        
        if (albumDTO.getArtistIds() == null || albumDTO.getArtistIds().isEmpty()) {
            throw new BusinessException("Pelo menos um artista é obrigatório");
        }
        
        List<Artist> artistList = artistRepository.findAllById(albumDTO.getArtistIds());

        if (artistList.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum artista encontrado com os IDs fornecidos");
        }

        Set<Artist> artists = new HashSet<>(artistList);
        Set<String> imageKeys = new HashSet<>();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile img : images) {
                if (img == null) {
                    continue;
                }
                
                if (img.isEmpty() || img.getSize() == 0) {
                    continue;
                }
                
                if (img.getOriginalFilename() != null && img.getOriginalFilename().equals("string")) {
                    continue;
                }
                
                if (img.getContentType() == null || !img.getContentType().startsWith("image/")) {
                    throw new BusinessException("Todos os arquivos devem ser imagens válidas (PNG, JPG, etc).");
                }
                
                if (img.getSize() > 5 * 1024 * 1024) { // 5MB
                    String filename = img.getOriginalFilename() != null ? img.getOriginalFilename() : "arquivo";
                    throw new BusinessException("A imagem " + filename + " excede 5MB.");
                }
                
                String key = minioService.uploadFile(img);
                imageKeys.add(key);
            }
        }

        Album album = Album.builder()
                .title(albumDTO.getTitle())
                .year(albumDTO.getYear())
                .artists(artists)
                .images(imageKeys)
                .build();

        Album savedAlbum = albumRepository.save(album);
        AlbumDTO dto = toDTO(savedAlbum);

        try {
            messagingTemplate.convertAndSend("/topic/albums", dto);
        } catch (Exception e) {
            log.warn("Erro ao enviar notificação WebSocket, continuando...", e);
        }

        return dto;
    }

    @Transactional
    @Override
    public AlbumDTO update(UUID id, AlbumDTO dto) {
        Album album = getEntityById(id);
        album.setTitle(dto.getTitle());
        album.setYear(dto.getYear());

        if (dto.getArtistIds() != null && !dto.getArtistIds().isEmpty()) {
            List<Artist> artists = artistRepository.findAllById(dto.getArtistIds());
            if (artists.isEmpty()) {
                throw new ResourceNotFoundException("Nenhum artista encontrado");
            }
            album.setArtists(new HashSet<>(artists));
        }
        return toDTO(albumRepository.save(album));
    }

    @Transactional
    @Override
    public void addCovers(UUID id, List<MultipartFile> files) {
        Album album = getEntityById(id);
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Nenhum arquivo enviado");
        }
        if (album.getImages() == null) {
            album.setImages(new HashSet<>());
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("Arquivo não pode ser vazio");
            }
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new BusinessException("Arquivo deve ser uma imagem");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException("Imagem excede 5MB");
            }
            album.getImages().add(minioService.uploadFile(file));
        }
        albumRepository.save(album);
    }

    @Override
    public List<String> getCoverUrls(UUID id) {
        Album album = getEntityById(id);
        if (album.getImages() == null || album.getImages().isEmpty()) {
            return List.of();
        }
        return album.getImages().stream()
                .map(minioService::getPresignedUrl)
                .collect(Collectors.toList());
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
        List<String> presignedUrls = null;
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            presignedUrls = album.getImages().stream()
                    .map(imageKey -> {
                        try {
                            return minioService.getPresignedUrl(imageKey);
                        } catch (Exception e) {
                            log.warn("Erro ao gerar URL para imagem: {}", imageKey, e);
                            return null;
                        }
                    })
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
        }

        return AlbumDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .year(album.getYear())
                .artistIds(album.getArtists().stream().map(Artist::getId).collect(Collectors.toList()))
                .artistNames(album.getArtists().stream().map(Artist::getName).collect(Collectors.toList()))
                .coverUrls(presignedUrls)
                .build();
    }
}
