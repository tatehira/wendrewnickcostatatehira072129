package com.wendrewnick.musicmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.tags.Tag;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Music Manager API", version = "1.0", description = "API para gerenciamento de Álbuns e Artistas.\n\n"
                +
                "### WebSocket\n" +
                "- **Topic:** `/topic/albums`\n" +
                "- **Descrição:** Notifica clientes em tempo real quando um novo álbum é criado.", contact = @Contact(name = "Wendrew Nick Costa Tatehira", email = "nickdebian@outlook.com")), servers = {
                                @Server(url = "/", description = "Default Server URL") }, tags = {
                                                @Tag(name = "Autenticação", description = "Endpoints para Login e atualização de Token"),
                                                @Tag(name = "Artistas", description = "Endpoints para gerenciamento de Artistas"),
                                                @Tag(name = "Álbuns", description = "Endpoints para gerenciamento de Álbuns"),
                                                @Tag(name = "Regionais", description = "Dados sincronizados de API externa"),
                                                @Tag(name = "Atuador", description = "Monitoramento e Interação"),
                                }, externalDocs = @ExternalDocumentation(description = "Documentação Completa", url = "https://github.com/tatehira/wendrewnickcostatatehira072129.git"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

        @org.springframework.context.annotation.Bean
        public org.springdoc.core.customizers.OpenApiCustomizer actuatorOpenApiCustomiser() {
                return openApi -> {
                        openApi.getPaths().entrySet().removeIf(entry -> {
                                String path = entry.getKey();
                                if (path.contains("**") || path.contains("{") && path.contains("}")) {
                                        return true;
                                }
                                return false;
                        });
                        
                        openApi.getPaths().forEach((path, pathItem) -> {
                                if (path.startsWith("/actuator")) {
                                        pathItem.readOperations().forEach(operation -> {
                                                if (operation.getTags() != null && operation.getTags().stream().anyMatch(t -> "Actuator".equalsIgnoreCase(t))) {
                                                        operation.setTags(java.util.List.of("Atuador"));

                                                        if (path.equals("/actuator")) {
                                                                operation.setSummary("Ponto de entrada do Atuador");
                                                                operation.setDescription("Lista todos os endpoints de monitoramento disponíveis.");
                                                        } else if (path.equals("/actuator/health") || path.equals("/actuator/health/liveness") || path.equals("/actuator/health/readiness")) {
                                                                operation.setSummary("Status de Saúde");
                                                                operation.setDescription("Verifica se a aplicação e suas dependências estão funcionando (UP/DOWN).");
                                                        } else if (path.startsWith("/actuator/info")) {
                                                                operation.setSummary("Informações da Aplicação");
                                                                operation.setDescription("Retorna detalhes sobre a versão e build da aplicação.");
                                                        } else if (path.startsWith("/actuator/metrics")) {
                                                                operation.setSummary("Métricas");
                                                                operation.setDescription("Exibe métricas de performance, memória e uso de recursos.");
                                                        } else if (path.startsWith("/actuator/prometheus")) {
                                                                operation.setSummary("Métricas Prometheus");
                                                                operation.setDescription("Expõe métricas no formato compatível com Prometheus.");
                                                        }
                                                }
                                        });
                                }
                        });
                        
                        if (openApi.getTags() != null) {
                                openApi.getTags().removeIf(tag -> "Actuator".equalsIgnoreCase(tag.getName()));
                        }
                };
        }
}
