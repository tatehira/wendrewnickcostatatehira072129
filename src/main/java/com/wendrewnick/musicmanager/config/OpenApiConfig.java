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
@OpenAPIDefinition(info = @Info(title = "Music Manager API", version = "1.0", description = "API para gerenciamento de Álbuns e Artistas.", contact = @Contact(name = "desenvolvedor", email = "nickdebian@outlook.com")), servers = {
                @Server(url = "/", description = "Default Server URL") }, tags = {
                                @Tag(name = "Autenticação", description = "Endpoints para Login e atualização de Token"),
                                @Tag(name = "Artistas", description = "Endpoints para gerenciamento de Artistas"),
                                @Tag(name = "Álbuns", description = "Endpoints para gerenciamento de Álbuns")
                }, externalDocs = @ExternalDocumentation(description = "Documentação Completa", url = "https://github.com/tatehira/wendrewnickcostatatehira072129.git"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
