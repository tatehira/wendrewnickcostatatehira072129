package com.wendrewnick.musicmanager.service;

import com.wendrewnick.musicmanager.dto.AuthRequest;
import com.wendrewnick.musicmanager.dto.AuthResponse;

public interface AuthService {
        AuthResponse authenticate(AuthRequest request);

        AuthResponse refreshToken(String refreshToken);
}
