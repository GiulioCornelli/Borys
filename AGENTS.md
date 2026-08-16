# AGENTS.md

Borys is a monorepo (no build tooling at the root) with 4 independent components. No CI, no lint/typecheck scripts. Docs and code messages are written in Italian — keep that style.

## MQTT broker (prerequisite for anything MQTT)

- `docker compose -f docker-compose.dev.yml up -d` starts the EMQX broker (`emqx1`).
- EMQX dashboard: http://localhost:18083 — creds come from `${EMQX_ADMIN_USER}` / `${EMQX_ADMIN_PASSWORD}` in the gitignored root `.env` (not hardcoded defaults).
- PostgreSQL and Grafana services are commented out in the compose file — not active.
- Ports: 1883 (MQTT), 8883 (MQTTS), 18083 (dashboard). MQTT creds `borys_agent` / `MqttPasswordSegreta2026` are used by the Python sim.

## Topic/payload conventions (used by EMQX/test and any MQTT work)

- Telemetry: `casa/{stanza}/{cond}/telemetry`, commands: `casa/{stanza}/{cond}/command`; subscribe to `casa/+/+/command`.
- Telemetry payload keys are snake_case: `temperature`, `mode`, `fan_speed`, `status`. Command payload uses `set_temp`, `mode`, `fan_speed`, `status`.

## EMQX/test (Python MQTT simulator)

- uv-managed (not pip). Python 3.12, `paho-mqtt>=2` (v2 API: `mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)`).
- Config via `.env` in `EMQX/test/` (copy `.env.example`); credentials optional — anonymous if omitted.
- `uv sync` then `uv run python publisher.py` (simulates 10 AC units in 3 rooms) or `uv run python subscriber.py` (subscribes to `casa/#`). Requires the EMQX broker up.

## Discord-iterface (Python: FastAPI + discord.py bot)

- uv-managed, Python 3.12. `uv sync` then `uv run main.py` (starts FastAPI server in a daemon thread + bot).
- Requires `.env` with `BORYS_TOKEN`, `SERVER_HOST`, `SERVER_PORT`. `app.py` disables `/docs` and `/redoc` when `PRODUCTION=true`.
- Bot prefix is `!`; cogs auto-load via `load_extension()` in `src/bots/bot.py` — new cog = add it to the `extensions` list there.

## borys-mcp-server (Quarkus, Java 21)

- MCP server on port 9100, root path `/mcp/v1` (streamable HTTP). Runs with `./mvnw quarkus:dev` (inside `borys-mcp-server/`).
- All tools live in `src/main/java/org/ai/borys/mcp/BorysMcpTools.java` and return fake data — no real hardware.
- Tool names must start with `get_`/`read_` (query) or `set_`/`exec_` (control): the ai-agent filters by prefix. Naming a new tool outside these prefixes makes it unreachable. Spec: `MCP_SERVER_SPECS.md`.

## borys-ai-agent (Quarkus, Java 21, LangChain4j)

- REST service on port 9090, endpoint `POST /api/chat` (`RestController.java`).
- Requires: `borys-mcp-server` running on 9100, and Ollama at `http://localhost:11434` with model `qwen2.5:3b` (configured in `src/main/resources/application.yml`).
- Flow: `RouterAgent` classifies (GETVALU/SETVALU/NAME/GENERAL) → routes to `QueryAgent` / `ControllerAgent`, each bound to the `borysmcp` MCP client via `@McpToolBox("borysmcp")`.

## Verification

- No test sources exist; verification is manual: run each service in dev mode and exercise `POST /api/chat`, or `./mvnw package` to confirm compilation.
- Full stack order: EMQX broker → mcp-server → ai-agent → Discord-iterface.
