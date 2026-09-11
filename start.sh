#!/usr/bin/env bash
# Avvia i servizi Quarkus (borys-mcp-server e borys-ai-agent) in modalità dev.
# L'output di entrambi viene intercettato e colorato per distinguere i due servizi.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Colori per distinguere i servizi
CYAN="\033[96m"
GREEN="\033[92m"
RESET="\033[0m"

cleanup() {
    echo -e "\n${RESET}Arresto servizi..."
    kill "$PID_MCP" "$PID_AGENT" 2>/dev/null || true
    wait "$PID_MCP" "$PID_AGENT" 2>/dev/null || true
    echo "Fatto."
}
trap cleanup EXIT INT TERM

echo -e "${CYAN}=== Avvio borys-mcp-server (porta 9100) ===${RESET}"
(cd "$ROOT_DIR/borys-mcp-server" && ./mvnw quarkus:dev -Dquarkus.http.port=9100) &
PID_MCP=$!

echo -e "${GREEN}=== Avvio borys-ai-agent (porta 9090) ===${RESET}"
(cd "$ROOT_DIR/borys-ai-agent" && ./mvnw quarkus:dev -Dquarkus.http.port=9090) &
PID_AGENT=$!

echo -e "Servizi avviati. Premi Ctrl+C per arrestare."
wait
