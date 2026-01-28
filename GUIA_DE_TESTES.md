# Guia de Testes - Music Manager API

Este documento descreve como testar todas as funcionalidades da API usando o Swagger UI.

## Índice

1. [Preparação](#1-preparação)
2. [Autenticação](#2-autenticação)
3. [Artistas](#3-artistas)
4. [Álbuns](#4-álbuns)
5. [Regionais](#5-regionais)
6. [Monitoramento](#6-monitoramento)
7. [Funcionalidades Extras](#7-funcionalidades-extras)

---

## 1. Preparação

### Iniciar o sistema

Execute no diretório raiz do projeto:

```bash
docker-compose up --build
```

Aguarde todos os serviços iniciarem. A API estará disponível em `http://localhost:8080`.

### Acessar o Swagger

Abra `http://localhost:8080` no navegador. Você será redirecionado automaticamente para o Swagger UI em `http://localhost:8080/swagger-ui.html`.

### Verificar saúde do sistema

No Swagger, procure pela tag "Atuador" e execute `GET /actuator/health`. Deve retornar status `UP`.

---

## 2. Autenticação

### Login

Execute `POST /api/v1/auth/login` com as credenciais:

```json
{
  "username": "admin",
  "password": "admin"
}
```

Copie o `accessToken` retornado. Você precisará dele para acessar os outros endpoints.

### Autorizar no Swagger

No topo da página do Swagger, clique em "Authorize" e cole o `accessToken` no campo "bearerAuth" (sem a palavra "Bearer"). Clique em "Authorize" e depois "Close".

### Refresh token

Para renovar o token, use `POST /api/v1/auth/refresh` enviando o `refreshToken` no header Authorization com o formato `Bearer <refresh-token>`.

---

## 3. Artistas

### Listar artistas

Execute `GET /api/v1/artists`. Você pode filtrar por nome usando o parâmetro `name` e ajustar a paginação com `page`, `size` e `sort`.

### Buscar por ID

Execute `GET /api/v1/artists/{id}` passando o UUID de um artista. Pegue um UUID da listagem anterior.

### Criar artista

Execute `POST /api/v1/artists` com:

```json
{
  "name": "The Beatles",
  "band": true
}
```

Crie alguns artistas (solo e bandas) para usar nos testes de álbuns.

### Atualizar artista

Execute `PUT /api/v1/artists/{id}` passando o UUID e os dados atualizados.

### Deletar artista

Execute `DELETE /api/v1/artists/{id}`. Retorna status 204 se bem-sucedido.

---

## 4. Álbuns

### Listar álbuns

Execute `GET /api/v1/albums`. Filtros disponíveis:
- `title`: filtrar por título
- `artistName`: filtrar por nome do artista
- `soloOrBand`: `true` para bandas, `false` para solos

### Buscar por ID

Execute `GET /api/v1/albums/{id}`. A resposta inclui `artistNames` e `coverUrls` se houver imagens.

### Criar álbum sem imagens

Execute `POST /api/v1/albums/simple` com:

```json
{
  "title": "Abbey Road",
  "year": 1969,
  "artistIds": ["uuid-do-artista-1", "uuid-do-artista-2"]
}
```

Use os UUIDs dos artistas criados anteriormente. Anote o `id` do álbum criado para os próximos passos.

### Criar álbum com imagens

Execute `POST /api/v1/albums` usando multipart/form-data:
- Campo `data`: JSON com `title`, `year` e `artistIds`
- Campo `images`: arquivo(s) de imagem

Nota: Se o Swagger der erro 415, use o método `/simple` primeiro e depois adicione as imagens.

### Adicionar capas a álbum existente

Execute `POST /api/v1/albums/{id}/covers` passando o UUID do álbum e selecionando um ou mais arquivos de imagem no campo `files`.

### Obter URLs das capas

Execute `GET /api/v1/albums/{id}/covers`. Retorna uma lista de URLs pré-assinadas válidas por 30 minutos. Copie uma URL e teste no navegador para verificar se a imagem abre corretamente.

### Atualizar álbum

Execute `PUT /api/v1/albums/{id}` passando o UUID e os dados atualizados.

### Deletar álbum

Execute `DELETE /api/v1/albums/{id}`. Retorna status 204 se bem-sucedido.

---

## 5. Regionais

### Listar regionais

Execute `GET /api/v1/regionals`. Filtros disponíveis:
- `nome`: filtrar por nome
- `id`: filtrar por ID
- `ativo`: `true` ou `false`

Os dados são sincronizados automaticamente a cada 1 minuto da API externa `integrador-argus-api.geia.vip`.

---

## 6. Monitoramento

### Health check

Execute `GET /actuator/health`. Deve retornar status `UP`.

### Informações da aplicação

Execute `GET /actuator/info` para ver informações sobre versão e build.

### Métricas

Execute `GET /actuator/metrics` para listar todas as métricas disponíveis.

### Métricas Prometheus

Execute `GET /actuator/prometheus` para obter métricas no formato Prometheus.

---

## 7. Funcionalidades Extras

### Rate limiting

O sistema limita a 10 requisições por minuto por padrão. Faça mais de 10 requisições em um minuto e a 11ª deve retornar status 429. Verifique os headers `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `Retry-After`.

### Tratamento de erros

Teste alguns cenários de erro:

- **Token inválido**: Remova o token do Swagger e tente acessar um endpoint protegido. Deve retornar 401.
- **Recurso não encontrado**: Busque um artista ou álbum com UUID inválido. Deve retornar 404 com mensagem clara.
- **Validação**: Tente criar um artista sem nome. Deve retornar 400 com detalhes dos erros.
- **Tipo de mídia não suportado**: Envie `application/octet-stream` para um endpoint que espera `multipart/form-data`. Deve retornar 415 com dica de como corrigir.

### WebSocket

Para testar notificações em tempo real:

1. Conecte em `ws://localhost:8080/ws` usando uma ferramenta como `wscat` ou extensão do Chrome
2. Inscreva-se no tópico `/topic/albums`
3. Crie um novo álbum via API
4. Você deve receber uma notificação com os dados do álbum criado

Comando para instalar e usar wscat:

```bash
npm install -g wscat
wscat -c ws://localhost:8080/ws
```

Depois envie:

```
SUBSCRIBE
id:sub-1
destination:/topic/albums

^@
```

### CORS

A API aceita requisições de `http://localhost:3000` e `http://localhost:8080`. Se você tiver um frontend rodando nessas origens, não deve ter problemas de CORS.

---

## Checklist de Testes

Use este checklist para garantir que testou tudo:

**Autenticação**
- [ ] Login com credenciais válidas
- [ ] Login com credenciais inválidas
- [ ] Refresh token
- [ ] Acesso sem token

**Artistas**
- [ ] Listar artistas (com e sem filtros)
- [ ] Buscar artista por ID
- [ ] Criar artista (solo e banda)
- [ ] Atualizar artista
- [ ] Deletar artista
- [ ] Validações

**Álbuns**
- [ ] Listar álbuns (com todos os filtros)
- [ ] Buscar álbum por ID
- [ ] Criar álbum sem imagens
- [ ] Criar álbum com imagens
- [ ] Adicionar capas a álbum existente
- [ ] Obter URLs das capas e testar no navegador
- [ ] Atualizar álbum
- [ ] Deletar álbum
- [ ] Validações

**Regionais**
- [ ] Listar regionais (com todos os filtros)
- [ ] Filtrar por ativo=true e ativo=false

**Monitoramento**
- [ ] Health check
- [ ] Info
- [ ] Metrics
- [ ] Prometheus

**Funcionalidades Extras**
- [ ] Rate limiting
- [ ] Tratamento de erros (401, 404, 400, 415)
- [ ] WebSocket (opcional)
- [ ] CORS (se tiver frontend)

---

## Problemas Comuns

**Erro 415 ao criar álbum com imagens**

Use o endpoint `/simple` para criar o álbum primeiro, depois adicione imagens via `/albums/{id}/covers`.

**Token expirado**

Faça login novamente ou use o endpoint `/auth/refresh`.

**MinIO não acessível**

Verifique se o container `music-minio` está rodando: `docker ps`

**Erro ao acessar URLs das capas**

Verifique se o MinIO está acessível em `http://localhost:9000` e se o bucket `music-covers` existe.

**Rate limit muito baixo**

Ajuste `rate-limit.requests-per-minute` no `application.yml` ou via variável de ambiente.

---

## Notas Importantes

- Todos os endpoints retornam respostas no formato `ApiResponse<T>` com `data`, `message` e `timestamp`
- Erros retornam `ProblemDetail` (RFC 7807) com informações estruturadas
- A API está versionada em `/api/v1`
- Todos os endpoints (exceto auth e actuator) requerem autenticação JWT
- O rate limit padrão é de 10 requisições por minuto
- As URLs das capas expiram em 30 minutos
