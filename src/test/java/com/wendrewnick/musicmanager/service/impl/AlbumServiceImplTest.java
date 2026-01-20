package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.entity.Album;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.BusinessException;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.AlbumRepository;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import com.wendrewnick.musicmanager.service.MinioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlbumServiceImpl albumService;

    @Test
    void create_ShouldReturnAlbumDTO_WhenSuccessful() {
        UUID artistId = UUID.randomUUID();
        Artist artist = Artist.builder().id(artistId).name("Artist Name").build();
        AlbumDTO inputDTO = AlbumDTO.builder()
                .title("Album Title")
                .year(2023)
                .artistIds(List.of(artistId))
                .build();

        Album savedAlbum = Album.builder()
                .id(UUID.randomUUID())
                .title("Album Title")
                .year(2023)
                .artists(Set.of(artist))
                .build();

        MultipartFile image = mock(MultipartFile.class);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        when(image.getSize()).thenReturn(1024L);
        when(minioService.uploadFile(image)).thenReturn("cover-key");
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);

        AlbumDTO result = albumService.create(inputDTO, image);

        assertNotNull(result);
        assertEquals("Album Title", result.getTitle());
        verify(minioService).uploadFile(image);
        verify(albumRepository).save(any(Album.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/albums"), any(AlbumDTO.class));
    }

    @Test
    void create_ShouldThrowException_WhenImageContentTypeInvalid() {
        UUID artistId = UUID.randomUUID();
        Artist artist = Artist.builder().id(artistId).build();
        AlbumDTO inputDTO = AlbumDTO.builder().artistIds(List.of(artistId)).build();

        MultipartFile image = mock(MultipartFile.class);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("application/pdf");

        assertThrows(BusinessException.class, () -> albumService.create(inputDTO, image));
    }

    @Test
    void create_ShouldThrowException_WhenImageSizeTooLarge() {
        UUID artistId = UUID.randomUUID();
        Artist artist = Artist.builder().id(artistId).build();
        AlbumDTO inputDTO = AlbumDTO.builder().artistIds(List.of(artistId)).build();

        MultipartFile image = mock(MultipartFile.class);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/png");
        when(image.getSize()).thenReturn(10 * 1024 * 1024L); // 10MB

        assertThrows(BusinessException.class, () -> albumService.create(inputDTO, image));
    }

    @Test
    void create_ShouldThrowException_WhenArtistNotFound() {
        UUID artistId = UUID.randomUUID();
        AlbumDTO inputDTO = AlbumDTO.builder().artistIds(List.of(artistId)).build();

        when(artistRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> albumService.create(inputDTO, null));
        verify(albumRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnAlbumDTO_WhenFound() {
        UUID id = UUID.randomUUID();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder()
                .id(id)
                .title("Title")
                .artists(Set.of(artist))
                .coverUrl("key")
                .build();

        when(albumRepository.findById(id)).thenReturn(Optional.of(album));
        when(minioService.getPresignedUrl("key")).thenReturn("http://url");

        AlbumDTO result = albumService.findById(id);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("http://url", result.getCoverUrl());
    }

    @Test
    void findAll_ShouldReturnPageOfAlbums() {
        Pageable pageable = Pageable.unpaged();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .artists(Set.of(artist))
                .build();

        when(albumRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(album)));

        Page<AlbumDTO> result = albumService.findAll(null, pageable);

        assertEquals(1, result.getSize());
    }
}
