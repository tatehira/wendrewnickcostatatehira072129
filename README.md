# Music Manager API

## 🎵 Sobre o Projeto
A **Music Manager API** é um sistema profissional para gerenciamento de Artistas e Álbuns.
O foco deste projeto é **simplicidade de execução** aliada a uma **arquitetura robusta** (Java, Spring Boot, Docker).

---

## 🚀 Guia de Instalação e Execução

Para garantir uma experiência limpa e livre de configurações complexas, utilize nossos scripts oficiais de inicialização.

### 🪟 Windows (Recomendado)

Utilize o inicializador oficial. Ele detecta se você possui as dependências (Docker) e as instala automaticamente se necessário.

1.  Abra a pasta do projeto.
2.  Execute o arquivo:
    > **`Iniciar-MusicManager.bat`**
3.  Uma janela segura de instalação será aberta. A primeira execução pode levar alguns minutos para configurar o ambiente.
4.  O sistema abrirá automaticamente no seu navegador quando estiver pronto.

### 🐧 Linux (Recomendado)

1.  No terminal, dentro da pasta do projeto, dê permissão de execução:
    ```bash
    chmod +x Iniciar-MusicManager.sh
    ```
2.  Execute o script:
    ```bash
    ./Iniciar-MusicManager.sh
    ```

---

### ⚙️ Execução Manual (Avançado)
Caso prefira gerenciar o ambiente Docker manualmente:

1.  Certifique-se de ter o **Docker** e **Docker Compose** instalados e rodando.
2.  No terminal, execute:
    ```bash
    docker compose up -d --build
    ```
3.  Aguarde os logs de inicialização e acesse:
    👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## 🛠️ Tecnologias
- **Java 17 & Spring Boot 3**: O coração da aplicação.
- **PostgreSQL**: Banco de dados robusto.
- **MinIO**: Armazenamento de imagens (compatível com S3).
- **Docker**: Para garantir que funcione em qualquer máquina.
- **Flyway**: Migrações de banco de dados seguras.

## 👤 Login Padrão
Para testar os endpoints protegidos:
- **Usuário:** `admin`
- **Senha:** `admin`
