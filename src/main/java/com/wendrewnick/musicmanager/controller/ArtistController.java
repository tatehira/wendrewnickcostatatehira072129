package com.wendrewnick.musicmanager.controller;

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
@Tag(name = "Artists", description = "Endpoints for managing Artists")
@SecurityRequirement(name = "bearerAuth")
public class ArtistController {

    private final ArtistService artistService;

    @Operation(summary = "List all artists", description = "Supports pagination and filtering by name")
    @GetMapping
    public ResponseEntity<Page<ArtistDTO>> getAllArtists(
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return ResponseEntity.ok(artistService.findAll(name, pageable));
    }

    @Operation(summary = "Get artist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable UUID id) {
        return ResponseEntity.ok(artistService.findById(id));
    }

    @Operation(summary = "Create a new artist")
    @PostMapping
    public ResponseEntity<ArtistDTO> createArtist(
            @Valid @RequestBody ArtistDTO artistDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.create(artistDTO));
    }

    @Operation(summary = "Update an artist")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistDTO> updateArtist(
            @PathVariable UUID id,
            @Valid @RequestBody ArtistDTO artistDTO
    ) {
        return ResponseEntity.ok(artistService.update(id, artistDTO));
    }

    @Operation(summary = "Delete an artist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable UUID id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
