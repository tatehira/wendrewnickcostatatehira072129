package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    @Operation(
            summary = "Criar um novo álbum",
            description = "Upload de múltiplas imagens e dados (multipart/form-data). Campo 'data' deve conter JSON com title, year e artistIds. Campo 'images' deve conter os arquivos de imagem.",
            requestBody = @RequestBody(
                    description = "Dados do álbum e imagens",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = AlbumDTO.class)
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbum(
            @Parameter(description = "Dados do álbum em JSON", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AlbumDTO.class))) @RequestPart(value = "data", required = false) AlbumDTO albumDTO,
            @Parameter(description = "Arquivos de imagem (PNG, JPG, etc)", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        
        if (albumDTO == null) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Campo 'data' é obrigatório. Envie os dados do álbum em formato JSON no campo 'data'.");
        }
        
        if (albumDTO.getTitle() == null || albumDTO.getTitle().isBlank()) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("O título é obrigatório");
        }
        
        if (albumDTO.getYear() == null) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("O ano é obrigatório");
        }
        
        if (albumDTO.getArtistIds() == null || albumDTO.getArtistIds().isEmpty()) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Pelo menos um artista é obrigatório");
        }
        
        AlbumDTO created = albumService.create(albumDTO, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Álbum criado com sucesso"));
    }

    @Operation(summary = "Criar um novo álbum (sem imagens)", description = "Cria álbum apenas com dados, sem upload de imagens")
    @PostMapping(value = "/simple", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AlbumDTO>> createAlbumSimple(
            @Valid @RequestBody AlbumDTO albumDTO) {
        if (albumDTO == null) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Dados do álbum não podem ser nulos");
        }
        
        log.debug("Recebido AlbumDTO - title: {}, year: {}, artistIds: {}", 
                albumDTO.getTitle(), albumDTO.getYear(), albumDTO.getArtistIds());
        
        AlbumDTO created = albumService.create(albumDTO, null);
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
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        if (files == null || files.isEmpty()) {
            throw new com.wendrewnick.musicmanager.exception.BusinessException("Campo 'files' é obrigatório. Envie pelo menos um arquivo de imagem.");
        }
        
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
