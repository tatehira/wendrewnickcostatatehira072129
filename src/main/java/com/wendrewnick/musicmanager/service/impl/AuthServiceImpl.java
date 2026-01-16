package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.AuthRequest;
import com.wendrewnick.musicmanager.dto.AuthResponse;
import com.wendrewnick.musicmanager.repository.UserRepository;
import com.wendrewnick.musicmanager.security.JwtService;
import com.wendrewnick.musicmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        var jwtToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Basic validation logic
        final String username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            var user = userRepository.findByUsername(username).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateAccessToken(user);
                // Ideally, rotate refresh token too, but simple re-issue of access token is
                // fine for this scope
                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken) // Return same refresh token or new one
                        .build();
            }
        }
        throw new RuntimeException("Refresh token inválido");
    }
}
