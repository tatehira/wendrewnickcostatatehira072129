package com.wendrewnick.musicmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wendrewnick.musicmanager.dto.AlbumDTO;
import com.wendrewnick.musicmanager.dto.ArtistDTO;
import com.wendrewnick.musicmanager.dto.AuthRequest;
import com.wendrewnick.musicmanager.dto.AuthResponse;
import com.wendrewnick.musicmanager.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlbumArtistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MinioService minioService;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = obtainAccessToken();
        when(minioService.uploadFile(any())).thenReturn("test-key");
        when(minioService.getPresignedUrl(any())).thenReturn("https://example.com/presigned-url");
    }

    private String obtainAccessToken() throws Exception {
        AuthRequest authRequest = new AuthRequest("admin", "admin");
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        AuthResponse response = objectMapper.readValue(body, AuthResponse.class);
        return response.getAccessToken();
    }

    @Test
    @DisplayName("GET /artists - deve listar artistas com paginação")
    void listarArtistas_DeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("POST /artists - deve criar artista e retornar 201")
    void criarArtista_DeveRetornar201() throws Exception {
        ArtistDTO dto = ArtistDTO.builder()
                .name("Artista Teste " + System.currentTimeMillis())
                .band(false)
                .build();

        mockMvc.perform(post("/api/v1/artists")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value(dto.getName()));
    }

    @Test
    @DisplayName("GET /artists com filtro - deve filtrar por nome")
    void listarArtistas_ComFiltroNome_DeveRetornarFiltrados() throws Exception {
        mockMvc.perform(get("/api/v1/artists")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("name", "admin")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /albums - deve listar álbuns com paginação")
    void listarAlbums_DeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/albums")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("POST /albums - deve criar álbum via JSON e retornar 201")
    void criarAlbum_DeveRetornar201() throws Exception {
        UUID artistId = criarArtistaERetornarId();

        AlbumDTO dto = AlbumDTO.builder()
                .title("Álbum Teste " + System.currentTimeMillis())
                .year(2024)
                .artistIds(List.of(artistId))
                .build();

        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.data.year").value(2024));
    }

    @Test
    @DisplayName("PUT /artists - deve atualizar artista")
    void atualizarArtista_DeveRetornar200() throws Exception {
        UUID artistId = criarArtistaERetornarId();
        ArtistDTO dto = ArtistDTO.builder()
                .name("Artista Atualizado")
                .band(true)
                .build();

        mockMvc.perform(put("/api/v1/artists/" + artistId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Artista Atualizado"));
    }

    @Test
    @DisplayName("GET /actuator/health/liveness - deve retornar UP")
    void healthLiveness_DeveRetornar200() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Requisição sem token - deve retornar 401")
    void endpointProtegido_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/artists"))
                .andExpect(status().isUnauthorized());
    }

    private UUID criarArtistaERetornarId() throws Exception {
        ArtistDTO dto = ArtistDTO.builder()
                .name("Artista para Álbum " + System.currentTimeMillis())
                .band(false)
                .build();

        String body = mockMvc.perform(post("/api/v1/artists")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String idStr = objectMapper.readTree(body).get("data").get("id").asText();
        return UUID.fromString(idStr);
    }
}
