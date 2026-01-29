# Modo Local (fallback sem Docker)

Este documento descreve **por que** existe o modo local, **em que situação** utilizá-lo e **como** executar a API sem Docker.

---

## Por que existe o fallback

O ambiente **oficial** da aplicação é o **modo Docker**: PostgreSQL e MinIO em containers, com o mesmo setup usado em avaliação e em time. Em alguns cenários, porém, **não é possível ou não é desejável** rodar Docker. O modo local existe para permitir rodar a API nesses casos, trocando apenas banco e storage, **sem alterar** regras de negócio, endpoints ou comportamento da API.

---

## Em que situação usar o fallback

Use o **modo local** quando:

| Situação | Motivo |
|----------|--------|
| **Docker indisponível** | Máquina sem Docker instalado (ex.: ambiente corporativo restrito, política de TI que bloqueia containers). |
| **Avaliação sem Docker** | Avaliador do projeto precisa testar a API mas não pode ou não quer usar Docker (ex.: apenas Java/Maven disponível). |
| **Desenvolvimento rápido** | Desenvolvedor quer subir a API na máquina local sem levantar PostgreSQL e MinIO (H2 em memória e arquivos em disco). |
| **CI/ambiente limitado** | Pipeline ou ambiente de teste onde só há JDK + Maven e não há permissão para Docker. |

**Quando não usar:** O modo local **não substitui** o ambiente Docker para avaliação oficial ou produção. O fluxo principal continua sendo `docker compose up`. Use o fallback apenas quando Docker não for uma opção.

---

## Pré-requisitos

Na máquina onde a API rodará (sem Docker):

| Ferramenta | Função | Onde baixar |
|------------|--------|-------------|
| **Java 21 (JDK)** | Executar e compilar a aplicação | [Eclipse Temurin (Adoptium) – Java 21 LTS](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jdk) (escolher instalador conforme sistema operacional) |
| **Apache Maven** | Build, dependências e execução (`mvn spring-boot:run`) | [Maven – Download](https://maven.apache.org/download.cgi) (Binary zip); em seguida configurar o `PATH` conforme a [documentação de instalação](https://maven.apache.org/install.html) |

**Verificação após instalação:**

- `java -version` → deve exibir versão 21.
- `mvn -v` → deve exibir Maven e o mesmo Java 21.

---

## Como usar

### 1. Ativar o profile `local`

A aplicação usa o profile Spring `local`, que carrega `application-local.yml` (H2 + storage em disco).

**Bash / CMD (Linux, macOS, Windows CMD):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**PowerShell (Windows):**  
No PowerShell, o `-D` é interpretado pelo shell. É necessário passar o parâmetro entre aspas:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Alternativa (qualquer shell):** definir a variável de ambiente e rodar em seguida:

```bash
# Linux / macOS
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

```powershell
# PowerShell
$env:SPRING_PROFILES_ACTIVE="local"; mvn spring-boot:run
```

### 2. Acessar a API

- **Base URL:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Actuator (health):** `http://localhost:8080/actuator/health`

Comportamento dos endpoints é o mesmo do modo Docker; apenas banco e armazenamento de imagens mudam.

---

## O que muda no modo local

| Aspecto | Modo Docker | Modo Local (fallback) |
|--------|-------------|------------------------|
| **Banco de dados** | PostgreSQL 16 (container) | H2 em memória, modo PostgreSQL |
| **Persistência do banco** | Volume/container | Nenhuma (dados perdidos ao encerrar o processo) |
| **Storage de imagens** | MinIO (S3-compatível) | Pasta `./local-storage` no diretório de execução |
| **URLs de capas** | URLs pré-assinadas (30 min) | URLs diretas: `http://localhost:8080/local-storage/{nome-do-arquivo}` |
| **Migrações** | Flyway contra PostgreSQL | Flyway contra H2 (mesmos scripts, modo PostgreSQL) |

A **lógica de negócio** (validações, regras, API REST, WebSocket, integração com API de regionais, rate limit, JWT) é **idêntica** nos dois modos. A troca é feita via **Spring Profiles** (`docker` / `local`) e uma **interface única de storage** (`StorageService`), com implementações distintas por profile.

---

## Configuração (application-local.yml)

O profile `local` é carregado a partir de `src/main/resources/application-local.yml`. Resumo das propriedades relevantes:

- **Datasource:** `jdbc:h2:mem:musicdb;MODE=PostgreSQL;...` (H2 em memória, compatível com os scripts Flyway em sintaxe PostgreSQL).
- **JPA:** dialect H2, `ddl-auto: validate`, Flyway habilitado.
- **Storage local:**
  - `storage.local.base-path`: diretório onde as imagens são salvas (default: `./local-storage`).
  - `storage.local.public-url`: base da URL pública das capas (default: `http://localhost:8080/local-storage`).

Para customizar (ex.: outro diretório ou porta), sobrescrever via variáveis de ambiente ou criar `application-local.yml` fora do JAR com as propriedades desejadas.

---

## Servindo arquivos em `/local-storage`

No profile `local`, o endpoint `GET /local-storage/{filename}` serve os arquivos gravados em `./local-storage`. Esse endpoint existe apenas quando o profile `local` está ativo; no modo Docker, as capas são acessadas via URLs pré-assinadas do MinIO.

---

## Resumo

| Item | Descrição |
|------|------------|
| **Quando usar** | Docker indisponível ou indesejado; avaliação/desenvolvimento apenas com Java e Maven. |
| **Comando (PowerShell)** | `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` |
| **Comando (Bash/CMD)** | `mvn spring-boot:run -Dspring-boot.run.profiles=local` |
| **URL da API** | `http://localhost:8080` |
| **Capas** | Salvas em `./local-storage`; acesso via `GET /local-storage/{filename}` |

O ambiente **oficial** para rodar e avaliar a aplicação continua sendo o **modo Docker** (ver [README.md](README.md) – Como rodar).
