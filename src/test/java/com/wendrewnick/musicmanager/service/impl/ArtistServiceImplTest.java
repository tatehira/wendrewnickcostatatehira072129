package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.BusinessException;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    void create_ShouldReturnArtistDTO_WhenSuccessful() {
        ArtistDTO inputDTO = ArtistDTO.builder().name("Pink Floyd").build();
        Artist savedArtist = Artist.builder().id(UUID.randomUUID()).name("Pink Floyd").build();

        when(artistRepository.existsByNameIgnoreCase("Pink Floyd")).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);

        ArtistDTO result = artistService.create(inputDTO);

        assertNotNull(result);
        assertEquals(savedArtist.getId(), result.getId());
        assertEquals("Pink Floyd", result.getName());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void create_ShouldThrowException_WhenNameExists() {
        ArtistDTO inputDTO = ArtistDTO.builder().name("Pink Floyd").build();

        when(artistRepository.existsByNameIgnoreCase("Pink Floyd")).thenReturn(true);

        assertThrows(BusinessException.class, () -> artistService.create(inputDTO));
        verify(artistRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnArtistDTO_WhenFound() {
        UUID id = UUID.randomUUID();
        Artist artist = Artist.builder().id(id).name("Queen").build();

        when(artistRepository.findById(id)).thenReturn(Optional.of(artist));

        ArtistDTO result = artistService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Queen", result.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(artistRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> artistService.findById(id));
    }

    @Test
    void update_ShouldReturnUpdatedDTO_WhenFound() {
        UUID id = UUID.randomUUID();
        ArtistDTO updateDTO = ArtistDTO.builder().name("Updated Name").build();
        Artist existingArtist = Artist.builder().id(id).name("Old Name").build();
        Artist updatedArtist = Artist.builder().id(id).name("Updated Name").build();

        when(artistRepository.findById(id)).thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class))).thenReturn(updatedArtist);

        ArtistDTO result = artistService.update(id, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void delete_ShouldCallRepository_WhenFound() {
        UUID id = UUID.randomUUID();
        when(artistRepository.existsById(id)).thenReturn(true);

        artistService.delete(id);

        verify(artistRepository).deleteById(id);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(artistRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> artistService.delete(id));
        verify(artistRepository, never()).deleteById(id);
    }

    @Test
    void findAll_ShouldReturnPage_WhenCalled() {
        Pageable pageable = Pageable.unpaged();
        List<Artist> artists = List.of(
                Artist.builder().id(UUID.randomUUID()).name("A1").build(),
                Artist.builder().id(UUID.randomUUID()).name("A2").build());
        Page<Artist> page = new PageImpl<>(artists);

        when(artistRepository.findAll(pageable)).thenReturn(page);

        Page<ArtistDTO> result = artistService.findAll(null, pageable);

        assertEquals(2, result.getContent().size());
    }
}
