package com.wendrewnick.musicmanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegionalResponseDTO {
    private Integer id;
    private String nome;
    private boolean ativo;
}
