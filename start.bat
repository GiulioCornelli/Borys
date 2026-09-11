@echo off
REM Avvia i servizi Quarkus (borys-mcp-server e borys-ai-agent) in modalita dev.
REM Ogni servizio apre una nuova finestra del terminale.

set ROOT_DIR=%~dp0

echo === Avvio borys-mcp-server (porta 9100) ===
start "borys-mcp-server" cmd /k "cd /d %ROOT_DIR%borys-mcp-server && call mvnw quarkus:dev -Dquarkus.http.port=9100"

echo === Avvio borys-ai-agent (porta 9090) ===
start "borys-ai-agent" cmd /k "cd /d %ROOT_DIR%borys-ai-agent && call mvnw quarkus:dev -Dquarkus.http.port=9090"

echo.
echo Servizi avviati. Chiudi le finestre per arrestare.
pause
