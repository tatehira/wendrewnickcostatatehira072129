package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.BusinessException;
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
        if (!pageable.getSort().isSorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("name").ascending());
        }

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
        if (artistRepository.existsByNameIgnoreCase(artistDTO.getName())) {
            throw new BusinessException("Já existe um artista cadastrado com este nome: " + artistDTO.getName());
        }
        boolean isBand = artistDTO.getBand() != null ? artistDTO.getBand() : true;
        Artist artist = Artist.builder()
                .name(artistDTO.getName())
                .band(isBand)
                .build();
        return toDTO(artistRepository.save(artist));
    }

    @Override
    public ArtistDTO update(UUID id, ArtistDTO artistDTO) {
        Artist artist = getEntityById(id);

        if (!artist.getName().equalsIgnoreCase(artistDTO.getName()) &&
                artistRepository.existsByNameIgnoreCase(artistDTO.getName())) {
            throw new BusinessException("Já existe um artista cadastrado com este nome: " + artistDTO.getName());
        }

        artist.setName(artistDTO.getName());
        if (artistDTO.getBand() != null) {
            artist.setBand(artistDTO.getBand());
        }
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
                .band(artist.isBand())
                .build();
    }
}
