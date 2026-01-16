# Configurações de UI
$host.UI.RawUI.WindowTitle = "Instalador - Music Manager API"
$ErrorActionPreference = "Stop"

function Show-Header {
    Clear-Host
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host "             MUSIC MANAGER API - INSTALADOR               " -ForegroundColor White
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Show-Step {
    param([string]$Message)
    Write-Host "[*] $Message..." -ForegroundColor Yellow -NoNewline
}

function Show-Success {
    Write-Host " [OK]" -ForegroundColor Green
    Start-Sleep -Seconds 1
}

function Show-Error {
    param([string]$Message)
    Write-Host " [ERRO]" -ForegroundColor Red
    Write-Host "    ! $Message" -ForegroundColor Red
    Write-Host ""
    Write-Host "Pressione qualquer tecla para sair..."
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Exit
}

# --- INICIO ---
Show-Header

# 1. Verificar Permissões de Admin
Show-Step "Verificando permissoes de administrador"
$identity = [System.Security.Principal.WindowsIdentity]::GetCurrent()
$principal = New-Object System.Security.Principal.WindowsPrincipal($identity)
if (-not $principal.IsInRole([System.Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host ""
    Write-Host " [!] Precisamos de permissao de administrador para configurar o ambiente." -ForegroundColor Yellow
    Write-Host "     O script sera reiniciado como Administrador." -ForegroundColor Cyan
    Start-Sleep -Seconds 2
    Start-Process powershell -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
    Exit
}
Show-Success

# 2. Verificar Dependências (Docker)
Show-Step "Verificando instalacao do Docker Desktop"
try {
    $dockerVersion = docker --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Show-Success
        Write-Host "    > Docker encontrado: $dockerVersion" -ForegroundColor Gray
    } else {
        throw "Docker nao encontrado"
    }
} catch {
    Write-Host " [AUSENTE]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "O Docker Desktop nao foi encontrado." -ForegroundColor White
    $response = Read-Host "Deseja baixar e instalar o Docker Desktop automaticamente? (S/N)"
    if ($response -eq 'S' -or $response -eq 's') {
        Write-Host ""
        Write-Host "Iniciando download e instalacao via Winget..." -ForegroundColor Cyan
        Write-Host "Isso pode levar alguns minutos. Uma janela de instalacao pode aparecer." -ForegroundColor Gray
        
        try {
            winget install -e --id Docker.DockerDesktop --accept-source-agreements --accept-package-agreements
            if ($LASTEXITCODE -ne 0) { throw "Falha na instalacao do Docker via Winget." }
            
            Write-Host ""
            Write-Host "Docker instalado com sucesso!" -ForegroundColor Green
            Write-Host "Instalacao requer reinicializacao do terminal/computador para aplicar grupos de usuario." -ForegroundColor Yellow
            Write-Host "Por favor, reinicie seu computador e execute este instalador novamente."
            Pause
            Exit
        } catch {
            Show-Error "Nao foi possivel instalar o Docker automaticamente. Por favor instale manualmente em docker.com"
        }
    } else {
        Show-Error "O Docker e necessario para rodar o sistema. Instalacao cancelada."
    }
}

# 3. Verificar se o Docker Daemon está rodando
Show-Step "Iniciando servicos do Docker"
$dockerInfo = docker info 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host " [INICIANDO]" -ForegroundColor Yellow
    
    # Tenta iniciar
    if (Test-Path "C:\Program Files\Docker\Docker\Docker Desktop.exe") {
        Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    } else {
        Show-Error "Nao encontrei o executavel do Docker. Abra-o manualmente."
    }

    # Loop de espera
    $retries = 0
    while ($retries -lt 30) {
        Write-Host "." -NoNewline -ForegroundColor Gray
        Start-Sleep -Seconds 2
        docker info >$null 2>&1
        if ($LASTEXITCODE -eq 0) {
            $dockerStarted = $true
            break
        }
        $retries++
    }
    
    if (-not $dockerStarted) {
        Show-Error "O Docker demorou muito para responder. Verifique se ele esta aberto na bandeja do sistema."
    }
}
Show-Success

# 4. Construção e Execução (Compose)
Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "             INSTALANDO O SISTEMA (BUILD)                 " -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Baixando imagens e compilando aplicacao..." -ForegroundColor Gray
Write-Host "(Isso pode demorar alguns minutos na primeira vez)" -ForegroundColor Gray
Write-Host ""

docker compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Show-Error "Houve um erro ao subir os conteineres."
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "                 INSTALACAO CONCLUIDA!                    " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

$url = "http://localhost:8080/swagger-ui.html"
Write-Host "Acessando sistema em: $url" -ForegroundColor Cyan

Start-Sleep -Seconds 2
Start-Process $url

Write-Host ""
Write-Host "Pressione qualquer tecla para fechar..."
$null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
