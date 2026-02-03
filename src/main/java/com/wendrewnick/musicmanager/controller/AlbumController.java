package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Operation(summary = "Listar álbuns", description = "Paginação. Filtros: title, artistName, soloOrBand (true=band, false=solo).")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlbumDTO>>> getAllAlbums(
            @Parameter(description = "Filtrar por título") @RequestParam(required = false) String title,
            @Parameter(description = "Filtrar por nome do artista") @RequestParam(required = false) String artistName,
            @Parameter(description = "true = bandas, false = artistas solo") @RequestParam(required = false) Boolean soloOrBand,
            @ParameterObject Pageable pageable) {
        Page<AlbumDTO> page = albumService.findAll(title, artistName, soloOrBand, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Álbuns listados com sucesso"));
    }

    @Operation(summary = "Buscar álbum por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDTO>> getAlbumById(@PathVariable UUID id) {
        AlbumDTO dto = albumService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Álbum encontrado"));
    }

    @Operation(summary = "Criar álbum (Com ou sem capas)", description = "Criar álbum enviando apenas JSON no body. Para enviar capas, use 'Criar álbum (multipart)'.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbumWithJson(@Valid @RequestBody AlbumDTO albumDTO) {
        AlbumDTO created = albumService.create(albumDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Álbum criado com sucesso"));
    }

    @Operation(summary = "Criar álbum (multipart)", description = "Criar álbum com dados na parte 'data' (JSON) e opcionalmente capas na parte 'images'.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbumWithMultipart(
            @Parameter(description = "Dados do álbum", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AlbumDTO.class))) @RequestPart("data") MultipartFile albumDTOFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        AlbumDTO albumDTO;
        try {
            albumDTO = objectMapper.readValue(albumDTOFile.getInputStream(), AlbumDTO.class);
        } catch (java.io.IOException e) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException(
                    "Erro ao processar JSON da parte 'data': " + e.getMessage());
        }

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
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(@PathVariable UUID id) {
        albumService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Álbum excluído com sucesso"));
    }
}
