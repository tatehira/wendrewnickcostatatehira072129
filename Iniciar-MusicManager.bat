@echo off
TITLE Inicializando...
REM Script Wrapper para iniciar o Instalador PowerShell com privilegios
REM Limpa a tela
cls
echo ======================================================
echo  Inicializando Instalador do Music Manager API...
echo ======================================================
echo.

REM Executa o script PowerShell ignorando politicas de execucao restritas
PowerShell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-core.ps1"

if %errorlevel% neq 0 (
    echo.
    echo Houve um erro na execucao.
    pause
)
