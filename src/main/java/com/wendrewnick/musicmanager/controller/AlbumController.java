package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Endpoints para gerenciamento de Álbuns")
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {

    private final AlbumService albumService;

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

    @Operation(summary = "Criar álbum (multipart)", description = "Criar álbum com campos individuais e anexar capa(s) abaixo.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbumWithMultipart(
            @Parameter(description = "Título do álbum") @RequestPart("title") String title,
            @Parameter(description = "Ano de lançamento (ex: 2024)") @RequestPart("year") String yearStr,
            @Parameter(description = "UUIDs dos artistas separados por vírgula (ex: uuid1,uuid2). Use GET /api/v1/artists", schema = @Schema(example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01")) @RequestPart("artistIds") String artistIds,
            @Parameter(description = "Capa(s) do álbum (imagens)") @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        List<UUID> artistIdList;
        try {
            artistIdList = Arrays.stream(artistIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException(
                    "artistIds inválido: use UUIDs separados por vírgula (ex: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01). " + e.getMessage());
        }
        if (artistIdList.isEmpty()) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Pelo menos um artista (artistIds) é obrigatório.");
        }

        int year;
        try {
            year = Integer.parseInt(yearStr.trim());
        } catch (NumberFormatException e) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Ano inválido: " + yearStr + ". Use um número (ex: 2024).");
        }
        if (year < 1900) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Ano deve ser maior ou igual a 1900.");
        }

        AlbumDTO albumDTO = AlbumDTO.builder()
                .title(title)
                .year(year)
                .artistIds(artistIdList)
                .build();

        // Filtra imagens vazias (ex.: quando Swagger envia part sem arquivo)
        List<MultipartFile> validImages = (images != null)
                ? images.stream().filter(f -> f != null && !f.isEmpty()).toList()
                : null;

        AlbumDTO created = albumService.create(albumDTO, (validImages != null && !validImages.isEmpty()) ? validImages : null);
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
