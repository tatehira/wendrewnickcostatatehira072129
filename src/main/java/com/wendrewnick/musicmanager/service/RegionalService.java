package com.wendrewnick.musicmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wendrewnick.musicmanager.dto.RegionalExternalDTO;
import com.wendrewnick.musicmanager.entity.Regional;
import com.wendrewnick.musicmanager.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalService {

    private final RegionalRepository regionalRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String API_URL = "https://integrador-argus-api.geia.vip/v1/regionais";

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        syncRegionals();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncRegionals() {
        log.info("Sincronizando regionais...");
        
        try {
            List<RegionalExternalDTO> remotos = fetchRegionals();
            List<Regional> locais = regionalRepository.findByAtivoTrue();
            
            Map<Integer, RegionalExternalDTO> mapaRemoto = remotos.stream()
                    .collect(Collectors.toMap(RegionalExternalDTO::id, Function.identity(),
                            (existing, replacement) -> existing));

            Map<Integer, Regional> mapaLocal = locais.stream()
                    .collect(Collectors.toMap(Regional::getRegionalId, Function.identity(),
                            (existing, replacement) -> existing));

            for (RegionalExternalDTO regional : remotos) {
                Regional local = mapaLocal.get(regional.id());
                
                if (local == null) {
                    salvarNovo(regional);
                } else if (!local.getNome().equals(regional.nome())) {
                    local.setAtivo(false);
                    regionalRepository.save(local);
                    salvarNovo(regional);
                }
            }

            for (Regional local : locais) {
                if (!mapaRemoto.containsKey(local.getRegionalId())) {
                    local.setAtivo(false);
                    regionalRepository.save(local);
                }
            }
            
            log.info("Sincronização concluída: {} regionais", remotos.size());

        } catch (Exception e) {
            log.error("Erro ao sincronizar regionais: {}", e.getMessage());
        }
    }

    private void salvarNovo(RegionalExternalDTO dto) {
        Regional novo = Regional.builder()
                .regionalId(dto.id())
                .nome(dto.nome())
                .ativo(true)
                .build();
        regionalRepository.save(novo);
    }

    private List<RegionalExternalDTO> fetchRegionals() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API externa retornou status " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public List<Regional> findAll() {
        return regionalRepository.findAll();
    }
}
