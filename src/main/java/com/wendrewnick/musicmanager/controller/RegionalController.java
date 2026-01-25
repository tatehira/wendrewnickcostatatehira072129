package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.RegionalResponseDTO;
import com.wendrewnick.musicmanager.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Dados sincronizados da API externa")
@SecurityRequirement(name = "bearerAuth")
public class RegionalController {

    private final RegionalService regionalService;

    @Operation(summary = "Listar regionais", description = "Retorna dados sincronizados da API externa. Filtros opcionais por nome, id e status.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegionalResponseDTO>>> findAll(
            @Parameter(description = "Filtrar por nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtrar por ID da regional") @RequestParam(required = false) Integer id,
            @Parameter(description = "Filtrar por status (true = ativo, false = inativo)") @RequestParam(required = false) Boolean ativo) {
        
        List<RegionalResponseDTO> list = regionalService.findWithFilters(nome, id, ativo).stream()
                .map(r -> RegionalResponseDTO.builder()
                        .id(r.getRegionalId())
                        .nome(r.getNome())
                        .ativo(r.isAtivo())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list, "Regionais listadas com sucesso"));
    }
}
