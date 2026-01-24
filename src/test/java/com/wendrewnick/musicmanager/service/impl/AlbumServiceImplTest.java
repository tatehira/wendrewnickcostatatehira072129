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
                .images(Set.of("cover-key"))
                .build();

        MultipartFile image = mock(MultipartFile.class);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");
        when(image.getSize()).thenReturn(1024L);
        when(minioService.uploadFile(image)).thenReturn("cover-key");
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);

        AlbumDTO result = albumService.create(inputDTO, List.of(image));

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
        when(image.getContentType()).thenReturn("application/pdf");

        List<MultipartFile> images = List.of(image);
        assertThrows(BusinessException.class, () -> albumService.create(inputDTO, images));
    }

    @Test
    void create_ShouldThrowException_WhenImageSizeTooLarge() {
        UUID artistId = UUID.randomUUID();
        Artist artist = Artist.builder().id(artistId).build();
        AlbumDTO inputDTO = AlbumDTO.builder().artistIds(List.of(artistId)).build();

        MultipartFile image = mock(MultipartFile.class);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(image.getContentType()).thenReturn("image/png");
        when(image.getSize()).thenReturn(10 * 1024 * 1024L); // 10MB

        List<MultipartFile> images = List.of(image);
        assertThrows(BusinessException.class, () -> albumService.create(inputDTO, images));
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
                .images(Set.of("key"))
                .build();

        when(albumRepository.findById(id)).thenReturn(Optional.of(album));
        when(minioService.getPresignedUrl("key")).thenReturn("http://url");

        AlbumDTO result = albumService.findById(id);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertTrue(result.getCoverUrls().contains("http://url"));
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

        Page<AlbumDTO> result = albumService.findAll(null, null, null, pageable);

        assertEquals(1, result.getSize());
    }

    @Test
    void findAll_ShouldFilterByTitle_WhenTitleProvided() {
        Pageable pageable = Pageable.unpaged();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder().id(UUID.randomUUID()).title("Harakiri").artists(Set.of(artist)).build();

        when(albumRepository.findByTitleContainingIgnoreCase("Hara", pageable))
                .thenReturn(new PageImpl<>(List.of(album)));

        Page<AlbumDTO> result = albumService.findAll("Hara", null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harakiri", result.getContent().get(0).getTitle());
    }

    @Test
    void findAll_ShouldFilterBySoloOrBand_WhenProvided() {
        Pageable pageable = Pageable.unpaged();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Band").band(true).build();
        Album album = Album.builder().id(UUID.randomUUID()).title("Album").artists(Set.of(artist)).build();

        when(albumRepository.findByArtistType(true, pageable))
                .thenReturn(new PageImpl<>(List.of(album)));

        Page<AlbumDTO> result = albumService.findAll(null, null, true, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_ShouldReturnUpdatedAlbum_WhenFound() {
        UUID albumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        Artist artist = Artist.builder().id(artistId).name("Artist").build();
        Album existingAlbum = Album.builder()
                .id(albumId)
                .title("Old Title")
                .year(2020)
                .artists(Set.of(artist))
                .build();

        AlbumDTO updateDTO = AlbumDTO.builder()
                .title("New Title")
                .year(2023)
                .artistIds(List.of(artistId))
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existingAlbum));
        when(artistRepository.findAllById(List.of(artistId))).thenReturn(List.of(artist));
        when(albumRepository.save(any(Album.class))).thenAnswer(inv -> inv.getArgument(0));

        AlbumDTO result = albumService.update(albumId, updateDTO);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals(2023, result.getYear());
    }

    @Test
    void update_ShouldThrowException_WhenAlbumNotFound() {
        UUID albumId = UUID.randomUUID();
        AlbumDTO updateDTO = AlbumDTO.builder().title("Title").build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> albumService.update(albumId, updateDTO));
    }

    @Test
    void delete_ShouldCallRepository_WhenAlbumExists() {
        UUID albumId = UUID.randomUUID();
        when(albumRepository.existsById(albumId)).thenReturn(true);

        albumService.delete(albumId);

        verify(albumRepository).deleteById(albumId);
    }

    @Test
    void delete_ShouldThrowException_WhenAlbumNotFound() {
        UUID albumId = UUID.randomUUID();
        when(albumRepository.existsById(albumId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> albumService.delete(albumId));
        verify(albumRepository, never()).deleteById(any());
    }

    @Test
    void addCovers_ShouldUploadFiles_WhenValid() {
        UUID albumId = UUID.randomUUID();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder()
                .id(albumId)
                .title("Album")
                .artists(Set.of(artist))
                .images(new java.util.HashSet<>())
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(minioService.uploadFile(file)).thenReturn("new-cover-key");

        albumService.addCovers(albumId, List.of(file));

        verify(minioService).uploadFile(file);
        verify(albumRepository).save(album);
        assertTrue(album.getImages().contains("new-cover-key"));
    }

    @Test
    void addCovers_ShouldThrowException_WhenNoFilesProvided() {
        UUID albumId = UUID.randomUUID();
        Album album = Album.builder().id(albumId).artists(Set.of()).build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        assertThrows(BusinessException.class, () -> albumService.addCovers(albumId, List.of()));
    }

    @Test
    void addCovers_ShouldThrowException_WhenFileNotImage() {
        UUID albumId = UUID.randomUUID();
        Album album = Album.builder().id(albumId).artists(Set.of()).images(new java.util.HashSet<>()).build();
        MultipartFile file = mock(MultipartFile.class);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(BusinessException.class, () -> albumService.addCovers(albumId, List.of(file)));
    }

    @Test
    void getCoverUrls_ShouldReturnPresignedUrls_WhenImagesExist() {
        UUID albumId = UUID.randomUUID();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder()
                .id(albumId)
                .title("Album")
                .artists(Set.of(artist))
                .images(Set.of("key1", "key2"))
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(minioService.getPresignedUrl("key1")).thenReturn("http://url1");
        when(minioService.getPresignedUrl("key2")).thenReturn("http://url2");

        List<String> urls = albumService.getCoverUrls(albumId);

        assertEquals(2, urls.size());
        assertTrue(urls.contains("http://url1"));
        assertTrue(urls.contains("http://url2"));
    }

    @Test
    void getCoverUrls_ShouldReturnEmptyList_WhenNoImages() {
        UUID albumId = UUID.randomUUID();
        Artist artist = Artist.builder().id(UUID.randomUUID()).name("Artist").build();
        Album album = Album.builder()
                .id(albumId)
                .title("Album")
                .artists(Set.of(artist))
                .images(null)
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        List<String> urls = albumService.getCoverUrls(albumId);

        assertTrue(urls.isEmpty());
    }
}
