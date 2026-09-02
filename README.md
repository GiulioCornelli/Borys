# Borys

Assistente AI per la gestione di sistemi IoT domestici/aziendali, con focus sul controllo dell'aria condizionata. L'utente interagisce via **Discord** con un agente AI (Qwen 2.5 3B) che può leggere le temperature, controllare i condizionatori e ricevere allarmi su temperature anomale.

## Architettura

```
Utente Discord
  → Discord-Interface (Python: FastAPI + discord.py)
    → borys-ai-agent (Quarkus + LangChain4j)
      → RouterAgent classifica l'intenzione
        → QueryAgent (lettura) / ControllerAgent (azione)
          → borys-mcp-server (tool @Tool MCP)
            → [futuro: API Gateway Go → dispositivi reali]
```

### Componenti

| Componente | Stack | Ruolo |
|---|---|---|
| **EMQX/test** | Python + paho-mqtt | Simulatore di 10 condizionatori (3 stanze) via MQTT |
| **Discord-Interface** | Python FastAPI + discord.py | Bot Discord, ponte verso Quarkus |
| **borys-mcp-server** | Quarkus (Java 21) | Server MCP con tool fittizzi che espone all'AI |
| **borys-ai-agent** | Quarkus + LangChain4j | Agenti AI (Router → Query/Controller) che usano i tool MCP |

## Prerequisiti

- Docker (per EMQX broker)
- Java 21 + Maven (per i componenti Quarkus)
- Python 3.12 + uv (per EMQX/test e Discord-Interface)
- Ollama con modello `qwen2.5:3b`
- Bot Discord (token nel `.env`)

## Avvio

### 1. Broker MQTT

```bash
docker compose -f docker-compose.dev.yml up -d
```

### 2. Simulatore dispositivi

```bash
cd EMQX/test
cp .env.example .env   # opzionale: credenziali MQTT
uv sync
uv run python publisher.py
```

### 3. MCP Server

```bash
cd borys-mcp-server
./mvnw quarkus:dev
```

Porta: 9100, path: `/mcp/v1`

### 4. AI Agent

```bash
cd borys-ai-agent
./mvnw quarkus:dev
```

Porta: 9090, endpoint: `POST /api/chat`

### 5. Bot Discord

```bash
cd Discord-iterface
# configura .env con BORYS_TOKEN, SERVER_HOST, SERVER_PORT
uv sync
uv run main.py
```

## Stack completo (futuro)

L'SRS prevede un'architettura a microservizi Go con:
- **InfluxDB** — storico temperature
- **PostgreSQL** — anagrafica dispositivi, dati real-time, utenti
- **Redis** — cache
- **Telemetry-Ingestion** — ingesta dati MQTT → DB
- **Device-Controller** — controllo condizionatori via MQTT
- **Query-Service** — esposizione dati + cache Redis
- **Auth-Admin-Service** — JWT + pannello admin (GoAdmin)
- **API-Gateway** — reverse proxy + auth
- **Stack Grafana** — Loki + Promtail + Dashboard