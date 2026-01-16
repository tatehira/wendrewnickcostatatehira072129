package com.wendrewnick.musicmanager.service;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistService artistService;

    @Test
    void findAll_ShouldReturnPageOfArtistDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Artist artist = new Artist(UUID.randomUUID(), "Test Artist", Collections.emptyList());
        Page<Artist> page = new PageImpl<>(Collections.singletonList(artist));

        when(artistRepository.findAll(pageable)).thenReturn(page);

        Page<ArtistDTO> result = artistService.findAll(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Artist", result.getContent().get(0).getName());
    }

    @Test
    void create_ShouldReturnArtistDTO() {
        ArtistDTO input = ArtistDTO.builder().name("New Artist").build();
        Artist savedArtist = new Artist(UUID.randomUUID(), "New Artist", Collections.emptyList());

        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);

        ArtistDTO result = artistService.create(input);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New Artist", result.getName());
    }

    @Test
    void delete_ShouldCallRepository_WhenExists() {
        UUID id = UUID.randomUUID();
        when(artistRepository.existsById(id)).thenReturn(true);

        artistService.delete(id);

        verify(artistRepository, times(1)).deleteById(id);
    }
}
