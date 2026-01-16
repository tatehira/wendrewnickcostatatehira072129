#!/bin/bash

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}==========================================================${NC}"
echo -e "${GREEN}             MUSIC MANAGER API - INSTALADOR (LINUX)       ${NC}"
echo -e "${GREEN}==========================================================${NC}"
echo ""

# 1. Verificar Docker
echo -e "${YELLOW}[*] Verificando instalação do Docker...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[ERRO] O Docker não foi encontrado.${NC}"
    echo -e "${YELLOW}Deseja tentar instalar o Docker automaticamente usando o script oficial? (s/n)${NC}"
    read -r install_docker
    if [[ "$install_docker" =~ ^([sS][sS]|[sS])$ ]]; then
         echo -e "${YELLOW}[*] Baixando e executando script de instalação do Docker...${NC}"
         curl -fsSL https://get.docker.com | sh
         if [ $? -ne 0 ]; then
             echo -e "${RED}[ERRO] Falha na instalação automática. Por favor instale manualmente.${NC}"
             exit 1
         fi
         echo -e "${GREEN}[OK] Docker instalado! Talvez seja necessário reiniciar a sessão ou usar 'sudo'.${NC}"
         echo -e "Tente rodar: sudo usermod -aG docker $USER"
    else
        echo -e "Instalação cancelada. O Docker é obrigatório."
        exit 1
    fi
else
    echo -e "${GREEN}[OK] Docker encontrado.${NC}"
fi

# 2. Verificar Permissões (Docker Daemon)
if ! docker info &> /dev/null; then
    echo -e "${YELLOW}[!] Não foi possível conectar ao Docker Daemon.${NC}"
    echo -e "Tentando executar com 'sudo'..."
    SUDO="sudo"
else
    SUDO=""
fi

# 3. Subir Aplicação
echo ""
echo -e "${GREEN}==========================================================${NC}"
echo -e "${GREEN}             INICIANDO O SISTEMA (BUILD)                  ${NC}"
echo -e "${GREEN}==========================================================${NC}"
echo -e "${YELLOW}Isso pode demorar alguns minutos na primeira vez...${NC}"
echo ""

$SUDO docker compose up -d --build

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}==========================================================${NC}"
    echo -e "${GREEN}                 INSTALAÇÃO CONCLUÍDA!                    ${NC}"
    echo -e "${GREEN}==========================================================${NC}"
    echo ""
    echo -e "API disponível em: ${GREEN}http://localhost:8080${NC}"
    echo -e "Swagger UI:        ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
    echo ""
else
    echo -e "${RED}[ERRO] Falha ao subir os serviços.${NC}"
fi
