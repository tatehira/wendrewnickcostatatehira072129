package com.wendrewnick.musicmanager.controller;

import com.wendrewnick.musicmanager.dto.ApiResponse;
import com.wendrewnick.musicmanager.dto.RegionalResponseDTO;
import com.wendrewnick.musicmanager.entity.Regional;
import com.wendrewnick.musicmanager.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Dados sincronizados de API externa")
@SecurityRequirement(name = "bearerAuth")
public class RegionalController {

    private final RegionalService regionalService;

    @Operation(summary = "Listar Regionais", description = "Retorna lista de regionais sincronizadas. Dados originados de API externa e versionados (ativo/inativo).")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegionalResponseDTO>>> getAllRegionals() {
        List<Regional> regionals = regionalService.findAll();
        List<RegionalResponseDTO> dtos = regionals.stream()
                .map(r -> RegionalResponseDTO.builder()
                        .id(r.getRegionalId())
                        .nome(r.getNome())
                        .ativo(r.isAtivo())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
