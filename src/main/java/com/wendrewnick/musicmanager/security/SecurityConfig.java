package com.wendrewnick.musicmanager.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;
        private final RateLimitFilter rateLimitFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                                        response.setCharacterEncoding("UTF-8");
                                                        response.getWriter()
                                                                        .write("""
                                                                                        {"title":"Não autenticado","detail":"Token de acesso ausente ou inválido. Faça login em /api/v1/auth/login","status":401}""");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                                        response.setCharacterEncoding("UTF-8");
                                                        response.getWriter()
                                                                        .write("""
                                                                                        {"title":"Acesso negado","detail":"Você não tem permissão para acessar este recurso.","status":403}""");
                                                }))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/api/v1/auth/**",
                                                                "/actuator/**",
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/ws/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
