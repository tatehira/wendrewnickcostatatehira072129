package com.wendrewnick.musicmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    @NotBlank(message = "O usuário é obrigatório")
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    private String password;
}
