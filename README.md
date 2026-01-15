# Music Manager API

## Visão Geral
API REST corporativa desenvolvida em Java com Spring Boot para gerenciamento de Artistas e Álbuns. O projeto foi desenhado para atender requisitos rigorosos de qualidade, segurança e escalabilidade, utilizando tecnologias modernas e containers.

O sistema permite o cadastro, edição, listagem e remoção de Artistas e seus respectivos Álbuns, incluindo upload de imagens de capa para Object Storage (MinIO) e geração de URLs assinadas (Presigned URLs) para acesso seguro.

## Arquitetura Adotada
O projeto segue uma **Arquitetura em Camadas** (Layered Architecture) para promover a separação de responsabilidades:

- **Controller**: Camada de entrada, responsável por receber requisições HTTP e validar dados (DTOs).
- **Service**: Núcleo da lógica de negócios, transações e orquestração.
- **Repository**: Camada de persistência, utilizando Spring Data JPA.
- **Entity**: Mapeamento Objeto-Relacional (ORM) das tabelas do banco.

**Padrões Utilizados:**
- DTO (Data Transfer Object) para dados de entrada e saída.
- Repository Pattern.
- Dependency Injection (Spring IoC).
- SOLID Principles.

## Tecnologias Utilizadas
- **Java 21**: Linguagem moderna e performática.
- **Spring Boot 3.2**: Framework base.
- **PostgreSQL**: Banco de dados relacional robusto.
- **Flyway**: Versionamento e migração de schema de banco de dados.
- **MinIO**: Object Storage compatível com S3 para armazenamento de imagens.
- **Spring Security + JWT**: Autenticação e Autorização Stateless.
- **Bucket4j**: Implementação de Rate Limiting.
- **Docker & Docker Compose**: Containerização e orquestração de ambiente.
- **Swagger / OpenAPI**: Documentação interativa da API.
- **Lombok**: Redução de boilerplate code.
- **Junit 5 & Mockito**: Testes Unitários.

## Pré-requisitos
- **Docker** e **Docker Compose** instalados.
- (Opcional) **Java 21** e **Maven** para execução local sem Docker.

## Como Rodar o Projeto

### Com Docker (Recomendado)
A maneira mais simples de rodar toda a infraestrutura (API, Banco e MinIO).

1. Na raiz do projeto, execute:
   ```bash
   docker-compose up --build
   ```
2. Aguarde a subida dos containers. A API estará disponível em `http://localhost:8080`.

### Localmente (Desenvolvimento)
1. Suba as dependências de infraestrutura:
   ```bash
   docker-compose up postgres minio createbuckets -d
   ```
2. Execute a aplicação via Maven ou IDE:
   ```bash
   ./mvnw spring-boot:run
   ```

## Endpoints Principais
Acesse a documentação completa via Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Autenticação
- `POST /api/v1/auth/login`: Recebe user/pass, retorna Access e Refresh Token.
- `POST /api/v1/auth/refresh`: Renova o Access Token.

### Artistas
- `GET /api/v1/artists`: Lista artistas (paginado, filtro por nome).
- `GET /api/v1/artists/{id}`: Detalhes do artista.
- `POST /api/v1/artists`: Cria artista.

### Álbuns
- `GET /api/v1/albums`: Lista álbuns (paginado, filtro por título).
- `POST /api/v1/albums`: Cria álbum com upload de imagem (`multipart/form-data`).

## Fluxo de Autenticação (JWT)
1. **Login**: O usuário envia credenciais para `/api/v1/auth/login`.
   - **Usuário Admin Default**: `username: admin`, `password: admin`.
2. **Tokens**: A API retorna um `access_token` (validade 5min) e um `refresh_token` (validade 24h).
3. **Uso**: Inclua o header `Authorization: Bearer <access_token>` em requisições protegidas.
4. **Renovação**: Quando o token expirar (403), use o `/refresh` com o cabeçalho `Authorization: Bearer <refresh_token>`.

## Upload de Imagens e MinIO
- O upload é feito via endpoint `POST /api/v1/albums`.
- A imagem é enviada para o container do **MinIO**.
- O banco salva apenas a **chave** do arquivo.
- Ao consultar um álbum, a API gera dinamicamente uma **Presigned URL** com validade de 30 minutos, permitindo que o front-end baixe a imagem diretamente do MinIO de forma segura.

## Health Check
Utilize os endpoints do Spring Actuator (não expostos por padrão neste escopo de segurança, mas configuráveis) ou um simples `GET /swagger-ui.html` para verificar se a aplicação está no ar.

## Decisões Técnicas
1. **UUID**: Utilizado para IDs para evitar enumeração sequencial e facilitar migração de dados.
2. **Flyway**: Escolhido para garantir que o banco de dados esteja sempre sincronizado com o código, essencial para CI/CD.
3. **MinIO Local**: Simula um ambiente real de Cloud (AWS S3) sem custos, mantendo a API agnóstica de provedor (S3 interface).
4. **Rate Limit em Memória**: Implementado via Bucket4j com filtro simples por IP para atender ao requisito sem complexidade excessiva de Redis (embora Redis fosse ideal para cluster).

## Limitações do Projeto
- **Auth Simplificada**: Usuários são gerenciados no banco, mas o registro de novos usuários não foi escopado (apenas login admin pré-criado).
- **Testes**: Cobertura foca na camada de Serviço (regras de negócio). Testes de integração (TestContainers) seriam o próximo passo ideal.