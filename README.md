# 🎵 Music Manager API

> **API REST Profissional para Gerenciamento de Artistas e Álbuns**
> 
> *Desenvolvido com foco em Arquitetura Corporativa, Clean Code e Segurança.*

---

## 📌 Visão Geral

Este projeto é uma **API RESTful** desenvolvida em **Java + Spring Boot** para resolver o desafio de gerenciamento de catálogo musical. 
Ele implementa padrões de mercado para garantir escalabilidade, segurança e manutenibilidade, servindo como uma base sólida para sistemas corporativos.

**Principais Funcionalidades:**
- ✅ **CRUD Completo** de Artistas e Álbuns.
- 🔐 **Autenticação JWT** (Access + Refresh Token) com Spring Security.
- 🛡️ **Rate Limiting** para proteção contra ataques de força bruta.
- 🖼️ **Upload de Imagens** integrado com MinIO (Compatível com AWS S3).
- 🐳 **Dockerizado** para execução agnóstica de ambiente.
- 📄 **Documentação Viva** com Swagger/OpenAPI.
- 🌍 **Internacionalização (i18n)** completa em Português (PT-BR).

---

## 🏗️ Arquitetura e Design

O projeto segue uma **Arquitetura em Camadas (Layered Architecture)** rigorosa para separar responsabilidades.

```mermaid
graph TD
    Client[Cliente (Web/Mobile)] -->|HTTP Request| Controller
    Controller -->|DTO| Service
    Service -->|Entity| Repository
    Repository -->|SQL| Database[(PostgreSQL)]
    Service -.->|File Stream| MinIO[(Object Storage)]
```

### 📂 Estrutura de Pastas (ASCII)
```text
src/main/java/com/wendrewnick/musicmanager
├── config/             # Configurações (Swagger, Security, RateLimit)
├── controller/         # Camada de Exposição (REST Endpoints)
├── dto/                # Objetos de Transferência de Dados (Inputs/Outputs)
├── entity/             # Entidades JPA (Mapeamento ORM)
├── exception/          # Tratamento Global de Erros (GlobalExceptionHandler)
├── repository/         # Camada de Acesso a Dados (Interfaces JPA)
└── service/            # Regras de Negócio e Interfaces
    └── impl/           # Implementação dos Serviços
```

---

## 🛠️ Stack Tecnológica

| Tecnologia | Versão | Função Principal |
| :--- | :--- | :--- |
| **Java** | 17 | Linguagem de programação (LTS). |
| **Spring Boot** | 3.x | Framework para desenvolvimento ágil. |
| **PostgreSQL** | 15+ | Banco de dados relacional robusto. |
| **Flyway** | 9.x | Versionamento (Migrations) do Banco de Dados. |
| **MinIO** | Latest | Object Storage para upload de imagens. |
| **Docker** | Latest | Orquestração de containers e ambiente. |
| **Spring Security** | 6.x | Segurança, Autenticação e Autorização. |
| **JWT** | 0.11.5 | JSON Web Token para sessões stateless. |
| **Bucket4j** | 8.x | Implementação de Rate Limiting. |
| **Lombok** | 1.18 | Redução de código boilerplate. |

---

## 🚀 Como Executar (Guia Passo-a-Passo)

Pré-requisito único: **Docker** instalado e rodando. Nada mais.

### 🪟 Windows

#### Opção A: Instalação Automática (Recomendada)
O inicializador configura tudo para você.
1. Abra a pasta do projeto.
2. Execute o arquivo: **`Iniciar-MusicManager.bat`**
3. Aguarde a mensagem de sucesso e o navegador abrirá automaticamente.

#### Opção B: Instalação Manual
```powershell
docker compose up -d --build
```

### 🐧 Linux

#### Opção A: Instalação Automática (Recomendada)
1. Dê permissão de execução e rode o script:
   ```bash
   chmod +x Iniciar-MusicManager.sh
   ./Iniciar-MusicManager.sh
   ```

#### Opção B: Instalação Manual
```bash
docker compose up -d --build
```

---

## 📚 Documentação da API

Após iniciar, acesse a documentação interativa completa (Swagger UI). Nela você pode testar todos os endpoints diretamente pelo navegador.

👉 **URL:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 🔑 Credenciais de Acesso (Seed)
O sistema cria automaticamente um usuário administrador na primeira execução:

- **Usuário:** `admin`
- **Senha:** `admin`

> **Nota:** Para acessar os endpoints protegidos no Swagger, faça login no endpoint `/auth`, copie o **Access Token**, clique no botão **Authorize** (cadeado) e cole o token.

---

## ✅ Decisões Técnicas

1.  **JWT com Refresh Token:** Decidimos implementar um fluxo completo de renovação de token para garantir segurança (tokens de acesso curtos) sem prejudicar a experiência do usuário (login persistente seguro).
2.  **MinIO para Uploads:** Em vez de salvar imagens no disco do servidor (o que quebraria em ambientes de nuvem efêmeros), utilizamos um Object Storage compatível com S3. Isso torna a migração para AWS S3 transparente.
3.  **Rate Limiting:** Implementado via filtro de Servlet para proteger a API de abusos, garantindo disponibilidade mesmo sob carga.
4.  **Installer Scripts:** Criamos scripts de inicialização (`.bat`/`.sh`) para abstrair a complexidade do Docker Compose para avaliadores ou usuários menos técnicos.

---

<p align="center">
  <i>Desenvolvido por Wendrew Nick Costa Tehira</i>
</p>
