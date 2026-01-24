package com.wendrewnick.musicmanager.service.impl;

import com.wendrewnick.musicmanager.dto.AuthRequest;
import com.wendrewnick.musicmanager.dto.AuthResponse;
import com.wendrewnick.musicmanager.entity.User;
import com.wendrewnick.musicmanager.exception.InvalidRefreshTokenException;
import com.wendrewnick.musicmanager.repository.UserRepository;
import com.wendrewnick.musicmanager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticate_ShouldReturnTokens_WhenCredentialsValid() {
        AuthRequest request = new AuthRequest("admin", "admin");
        User user = User.builder().id(UUID.randomUUID()).username("admin").password("encoded").build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenCredentialsInvalid() {
        AuthRequest request = new AuthRequest("admin", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken_WhenRefreshTokenValid() {
        User user = User.builder().id(UUID.randomUUID()).username("admin").build();
        String validRefresh = "valid-refresh-token";

        when(jwtService.extractUsername(validRefresh)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(validRefresh, user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(validRefresh);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(validRefresh, response.getRefreshToken());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUsernameNull() {
        when(jwtService.extractUsername("invalid-token")).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken("invalid-token"));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserNotFound() {
        when(jwtService.extractUsername("token")).thenReturn("unknown");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken("token"));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenExpired() {
        User user = User.builder().id(UUID.randomUUID()).username("admin").build();

        when(jwtService.extractUsername("expired-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("expired-token", user)).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken("expired-token"));
    }
}
