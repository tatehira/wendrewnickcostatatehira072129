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
    
    private static final long TAMANHO_MAX_IMAGEM = 5 * 1024 * 1024; // 5MB

    @Transactional(readOnly = true)
    @Override
    public Page<AlbumDTO> findAll(String title, String artistName, Boolean soloOrBand, Pageable pageable) {
        Page<Album> albuns;
        
        if (title != null && !title.isBlank()) {
            albuns = albumRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (artistName != null && !artistName.isBlank()) {
            albuns = albumRepository.findByArtistsNameContainingIgnoreCase(artistName, pageable);
        } else if (soloOrBand != null) {
            albuns = albumRepository.findByArtistType(soloOrBand, pageable);
        } else {
            albuns = albumRepository.findAll(pageable);
        }
        
        return albuns.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public AlbumDTO findById(UUID id) {
        return toDTO(buscarPorId(id));
    }

    @Transactional
    @Override
    public AlbumDTO create(AlbumDTO albumDTO, List<MultipartFile> images) {
        List<Artist> artistas = artistRepository.findAllById(albumDTO.getArtistIds());
        if (artistas.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum artista encontrado com os IDs fornecidos");
        }

        Set<String> chavesImagens = new HashSet<>();
        if (images != null) {
            for (MultipartFile imagem : images) {
                validarImagem(imagem);
                chavesImagens.add(minioService.uploadFile(imagem));
            }
        }

        Album album = Album.builder()
                .title(albumDTO.getTitle())
                .year(albumDTO.getYear())
                .artists(new HashSet<>(artistas))
                .images(chavesImagens)
                .build();

        Album salvo = albumRepository.save(album);
        AlbumDTO dto = toDTO(salvo);

        messagingTemplate.convertAndSend("/topic/albums", dto);

        return dto;
    }
    
    private void validarImagem(MultipartFile imagem) {
        String tipo = imagem.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new BusinessException("Arquivo inválido. Envie uma imagem (PNG, JPG, etc)");
        }
        if (imagem.getSize() > TAMANHO_MAX_IMAGEM) {
            throw new BusinessException("Imagem excede o limite de 5MB: " + imagem.getOriginalFilename());
        }
    }

    @Transactional
    @Override
    public AlbumDTO update(UUID id, AlbumDTO dto) {
        Album album = buscarPorId(id);
        album.setTitle(dto.getTitle());
        album.setYear(dto.getYear());

        if (dto.getArtistIds() != null && !dto.getArtistIds().isEmpty()) {
            List<Artist> artistas = artistRepository.findAllById(dto.getArtistIds());
            if (artistas.isEmpty()) {
                throw new ResourceNotFoundException("Nenhum artista encontrado");
            }
            album.setArtists(new HashSet<>(artistas));
        }
        
        return toDTO(albumRepository.save(album));
    }

    @Transactional
    @Override
    public void addCovers(UUID id, List<MultipartFile> files) {
        Album album = buscarPorId(id);
        
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Nenhum arquivo enviado");
        }
        
        if (album.getImages() == null) {
            album.setImages(new HashSet<>());
        }
        
        for (MultipartFile arquivo : files) {
            validarImagem(arquivo);
            album.getImages().add(minioService.uploadFile(arquivo));
        }
        
        albumRepository.save(album);
    }

    @Override
    public List<String> getCoverUrls(UUID id) {
        Album album = buscarPorId(id);
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
            throw new ResourceNotFoundException("Álbum não encontrado: " + id);
        }
        albumRepository.deleteById(id);
    }

    private Album buscarPorId(UUID id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado: " + id));
    }

    private AlbumDTO toDTO(Album album) {
        List<String> urlsCapas = null;
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            urlsCapas = album.getImages().stream()
                    .map(minioService::getPresignedUrl)
                    .collect(Collectors.toList());
        }

        return AlbumDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .year(album.getYear())
                .artistIds(album.getArtists().stream().map(Artist::getId).collect(Collectors.toList()))
                .artistNames(album.getArtists().stream().map(Artist::getName).collect(Collectors.toList()))
                .coverUrls(urlsCapas)
                .build();
    }
}
