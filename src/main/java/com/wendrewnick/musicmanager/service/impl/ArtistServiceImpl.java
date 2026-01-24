package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.entity.Artist;
import com.wendrewnick.musicmanager.exception.BusinessException;
import com.wendrewnick.musicmanager.exception.ResourceNotFoundException;
import com.wendrewnick.musicmanager.repository.ArtistRepository;
import com.wendrewnick.musicmanager.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;

    @Override
    public Page<ArtistDTO> findAll(String name, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("name"));
        }

        Page<Artist> artistas;
        if (name != null && !name.isBlank()) {
            artistas = artistRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            artistas = artistRepository.findAll(pageable);
        }
        
        return artistas.map(this::toDTO);
    }

    @Override
    public ArtistDTO findById(UUID id) {
        return toDTO(buscarPorId(id));
    }

    @Override
    public ArtistDTO create(ArtistDTO dto) {
        if (artistRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Já existe artista com esse nome");
        }
        
        boolean ehBanda = dto.getBand() != null ? dto.getBand() : true;
        
        Artist artista = Artist.builder()
                .name(dto.getName())
                .band(ehBanda)
                .build();
                
        return toDTO(artistRepository.save(artista));
    }

    @Override
    public ArtistDTO update(UUID id, ArtistDTO dto) {
        Artist artista = buscarPorId(id);

        if (!artista.getName().equalsIgnoreCase(dto.getName()) 
                && artistRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Já existe artista com esse nome");
        }

        artista.setName(dto.getName());
        if (dto.getBand() != null) {
            artista.setBand(dto.getBand());
        }
        
        return toDTO(artistRepository.save(artista));
    }

    @Override
    public void delete(UUID id) {
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Artista não encontrado: " + id);
        }
        artistRepository.deleteById(id);
    }

    private Artist buscarPorId(UUID id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado: " + id));
    }

    private ArtistDTO toDTO(Artist artista) {
        return ArtistDTO.builder()
                .id(artista.getId())
                .name(artista.getName())
                .band(artista.isBand())
                .build();
    }
}
