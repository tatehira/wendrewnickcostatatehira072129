package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import com.wendrewnick.musicmanager.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;

    @Override
    public Page<ArtistDTO> findAll(String name, Pageable pageable) {
        Page<Artist> artists;
        if (name != null && !name.isBlank()) {
            artists = artistRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            artists = artistRepository.findAll(pageable);
        }
        return artists.map(this::toDTO);
    }

    @Override
    public ArtistDTO findById(UUID id) {
        return toDTO(getEntityById(id));
    }

    @Override
    public ArtistDTO create(ArtistDTO artistDTO) {
        Artist artist = Artist.builder()
                .name(artistDTO.getName())
                .build();
        return toDTO(artistRepository.save(artist));
    }

    @Override
    public ArtistDTO update(UUID id, ArtistDTO artistDTO) {
        Artist artist = getEntityById(id);
        artist.setName(artistDTO.getName());
        return toDTO(artistRepository.save(artist));
    }

    @Override
    public void delete(UUID id) {
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Artista não encontrado com o ID: " + id);
        }
        artistRepository.deleteById(id);
    }

    private Artist getEntityById(UUID id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com o ID: " + id));
    }

    private ArtistDTO toDTO(Artist artist) {
        return ArtistDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .build();
    }
}
