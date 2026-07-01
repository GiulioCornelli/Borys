# Borys — AGENTS.md

## Dev environment
- `docker compose -f docker-compose.dev.yml up -d` — starts EMQX MQTT broker
- `.env` (gitignored) holds EMQX admin creds and MQTT user list
- EMQX dashboard: http://0.0.0.0:18083 (default: `admin` / `pippo123`)

## MQTT test scripts
- Python scripts in `EMQX/test/`, managed with **uv** (not pip)
- `uv sync` — install deps (paho-mqtt)
- `uv run python publisher.py` — publishes random temps to `/casa/stanza*/cond*/`
- `uv run python subscriber.py` — subscribes to `/casa/#` on `localhost:1883`
- Requires `emqx1` service running

## Stack notes
- No build/lint/typecheck scripts configured yet
- Go-related entries in `.gitignore` suggest future Go backend
- PostgreSQL service is commented out in docker-compose (planned but not active)
- No monorepo tooling; single flat layout
