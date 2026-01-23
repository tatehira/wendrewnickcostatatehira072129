package com.wendrewnick.musicmanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AlbumCoverDTO {
    private String url;
    private Instant expiresAt;
}
