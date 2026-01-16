# Music Manager API

## 🎵 Sobre o Projeto
A **Music Manager API** é um sistema profissional para gerenciamento de Artistas e Álbuns.
O foco deste projeto é **simplicidade de execução** aliada a uma **arquitetura robusta** (Java, Spring Boot, Docker).

---

## 🚀 Guia de Instalação e Execução

Escolha seu sistema operacional e o método de instalação preferido.

### 🪟 Windows

#### Opção A: Instalação Automática (Recomendada)
O inicializador detecta se você possui o Docker e, se não tiver, instala tudo para você.

1.  Abra a pasta do projeto.
2.  Dê dois cliques no arquivo:
    > **`Iniciar-MusicManager.bat`**
3.  Aguarde a janela de configuração. O sistema abrirá no navegador automaticamente.

#### Opção B: Instalação Manual (Avançado)
Se você já é desenvolvedor e prefere usar o terminal:

1.  Certifique-se de ter o **Docker Desktop** instalado e rodando.
2.  Abra o PowerShell na pasta do projeto.
3.  Execute:
    ```powershell
    docker compose up -d --build
    ```
4.  Aguarde os containers subirem.

---

### 🐧 Linux

#### Opção A: Instalação Automática (Recomendada)
Script automatizado para facilitar o setup no Linux.

1.  No terminal, navegue até a pasta do projeto.
2.  Dê permissão e execute:
    ```bash
    chmod +x Iniciar-MusicManager.sh
    ./Iniciar-MusicManager.sh
    ```

#### Opção B: Instalação Manual (Avançado)
Para quem prefere controlar o Docker manualmente via CLI:

1.  Certifique-se de ter **Docker** e **Docker Compose** instalados.
2.  No terminal, execute:
    ```bash
    docker compose up -d --build
    ```

---

## 📚 Acessando o Sistema
Após a instalação (qualquer método), acesse a documentação interativa:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## 🛠️ Tecnologias Utilizadas
- **Java 17 / Spring Boot 3**: API robusta e moderna.
- **PostgreSQL**: Banco de dados relacional.
- **MinIO**: Object Storage para imagens (S3 Compatible).
- **Docker**: Containerização completa.
- **Flyway**: Versionamento de Banco de Dados.

## 👤 Login Padrão
Use estas credenciais para acessar os endpoints protegidos:
- **Usuário:** `admin`
- **Senha:** `admin`
