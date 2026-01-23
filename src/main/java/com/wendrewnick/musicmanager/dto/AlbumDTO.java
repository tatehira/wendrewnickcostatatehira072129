package com.wendrewnick.musicmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumDTO {

    private UUID id;

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 2, max = 100, message = "O título deve ter entre 2 e 100 caracteres")
    private String title;

    @NotNull(message = "O ano é obrigatório")
    @Min(value = 1900, message = "O ano deve ser válido")
    private Integer year;

    @NotNull(message = "Pelo menos um Artista é obrigatório")
    private List<UUID> artistIds;

    private List<String> artistNames;
    private List<String> coverUrls;
}
