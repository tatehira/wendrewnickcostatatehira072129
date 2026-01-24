package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Endpoints para gerenciamento de Álbuns")
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "Listar todos os álbuns", description = "Suporta paginação e filtro por título ou nome do artista")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlbumDTO>>> getAllAlbums(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artistName,
            Pageable pageable) {
        Page<AlbumDTO> page = albumService.findAll(title, artistName, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Álbuns recuperados com sucesso"));
    }

    @Operation(summary = "Buscar álbum por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDTO>> getAlbumById(@PathVariable UUID id) {
        AlbumDTO dto = albumService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Álbum encontrado"));
    }

    @Operation(summary = "Criar um novo álbum", description = "Upload de múltiplas imagens e dados")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbum(
            @Parameter(description = "Dados do álbum", content = @Content(mediaType = "application/json")) @RequestPart("data") @Valid AlbumDTO albumDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        AlbumDTO created = albumService.create(albumDTO, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Álbum criado com sucesso"));
    }

    @Operation(summary = "Atualizar um álbum")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDTO>> updateAlbum(
            @PathVariable UUID id,
            @Valid @RequestBody AlbumDTO albumDTO) {
        AlbumDTO updated = albumService.update(id, albumDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Álbum atualizado"));
    }

    @Operation(summary = "Adicionar capas ao álbum", description = "Upload de imagens (multipart)")
    @PostMapping(value = "/{id}/covers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> addCovers(
            @PathVariable UUID id,
            @RequestPart("files") List<MultipartFile> files) {
        albumService.addCovers(id, files);
        return ResponseEntity.ok(ApiResponse.success(null, "Capas adicionadas"));
    }

    @Operation(summary = "Obter URLs das capas", description = "Retorna URLs pré-assinadas (30min)")
    @GetMapping("/{id}/covers")
    public ResponseEntity<ApiResponse<List<String>>> getCoverUrls(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getCoverUrls(id)));
    }

    @Operation(summary = "Deletar um álbum")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
