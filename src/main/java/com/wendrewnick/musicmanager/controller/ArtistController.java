package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Endpoints para gerenciamento de Artistas")
@SecurityRequirement(name = "bearerAuth")
public class ArtistController {

    private final ArtistService artistService;

    @Operation(summary = "Listar artistas", description = "Paginação e filtro por nome. Ordenação alfabética: ?sort=name,asc ou ?sort=name,desc (default: name,asc).")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArtistDTO>>> getAllArtists(
            @RequestParam(required = false) String name,
            @org.springframework.data.web.PageableDefault(sort = "name", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable) {
        Page<ArtistDTO> page = artistService.findAll(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Artistas recuperados com sucesso"));
    }

    @Operation(summary = "Buscar artista por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistDTO>> getArtistById(@PathVariable UUID id) {
        ArtistDTO dto = artistService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Artista encontrado"));
    }

    @Operation(summary = "Criar um novo artista")
    @PostMapping
    public ResponseEntity<ApiResponse<ArtistDTO>> createArtist(
            @Valid @RequestBody ArtistDTO artistDTO) {
        ArtistDTO created = artistService.create(artistDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Artista criado com sucesso"));
    }

    @Operation(summary = "Atualizar um artista")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistDTO>> updateArtist(
            @PathVariable UUID id,
            @Valid @RequestBody ArtistDTO artistDTO) {
        ArtistDTO updated = artistService.update(id, artistDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Artista atualizado com sucesso"));
    }

    @Operation(summary = "Deletar um artista")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable UUID id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
