package com.wendrewnick.musicmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wendrewnick.musicmanager.dto.RegionalExternalDTO;
import com.wendrewnick.musicmanager.entity.Regional;
import com.wendrewnick.musicmanager.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class) // Executa ao iniciar
    @Scheduled(fixedRate = 60000) // Executa a cada 1 minuto
    @Transactional
    public void syncRegionals() {
        log.info("Iniciando sincronização de regionais...");
        try {
            List<RegionalExternalDTO> remoteList = fetchRegionals();
            Map<Integer, RegionalExternalDTO> remoteMap = remoteList.stream()
                    .collect(Collectors.toMap(RegionalExternalDTO::id, Function.identity()));

            List<Regional> localActiveList = regionalRepository.findByAtivoTrue();
            Map<Integer, Regional> localMap = localActiveList.stream()
                    .collect(Collectors.toMap(Regional::getRegionalId, Function.identity()));

            for (RegionalExternalDTO remote : remoteList) {
                Regional local = localMap.get(remote.id());
                if (local == null) {

                    createRegional(remote);
                } else if (!local.getNome().equals(remote.nome())) {

                    local.setAtivo(false);
                    regionalRepository.save(local);
                    createRegional(remote);
                }
            }

            for (Regional local : localActiveList) {
                if (!remoteMap.containsKey(local.getRegionalId())) {

                    local.setAtivo(false);
                    regionalRepository.save(local);
                }
            }
            log.info("Sincronização concluída.");

        } catch (Exception e) {
            log.error("Erro ao sincronizar regionais: ", e);
        }
    }

    private void createRegional(RegionalExternalDTO dto) {
        Regional newRegional = Regional.builder()
                .regionalId(dto.id())
                .nome(dto.nome())
                .ativo(true)
                .build();
        regionalRepository.save(newRegional);
        log.debug("Regional criada/atualizada: {}", dto.nome());
    }

    private List<RegionalExternalDTO> fetchRegionals() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Falha na API externa: " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }
}
