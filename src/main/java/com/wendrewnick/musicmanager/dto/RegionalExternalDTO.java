package com.wendrewnick.musicmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegionalExternalDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("nome") String nome) {
}
