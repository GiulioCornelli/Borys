# MQTT Test — Publisher / Subscriber

Script per testare il broker EMQX con un pool di 10 condizionatori virtuali.

## Prerequisiti

- [uv](https://docs.astral.sh/uv/) (>= 0.4)
- Broker EMQX in esecuzione (da `docker compose -f docker-compose.dev.yml up -d`)

## Setup

```sh
uv sync
```

Installa `paho-mqtt` nella virtualenv gestita da uv.

### Configurazione

Crea un file `.env` nella cartella `EMQX/test/` (usa `.env.example` come template):

```env
MQTT_BROKER_HOST=localhost
MQTT_BROKER_PORT=1883
MQTT_USERNAME=borys_agent
MQTT_PASSWORD=MqttPasswordSegreta2026
```

`MQTT_USERNAME` e `MQTT_PASSWORD` sono opzionali — se omessi, la connessione è anonima.

## publisher.py

Simula 10 condizionatori distribuiti in 3 stanze:

| Stanza   | Condizionatori                     |
|----------|------------------------------------|
| stanza1  | condi_stanza1_1 … condi_stanza1_4 |
| stanza2  | condi_stanza2_1 … condi_stanza2_3 |
| stanza3  | condi_stanza3_1 … condi_stanza3_3 |

**Cosa fa:**
- All'avvio si connette a `localhost:1883` e si sottoscrive a `casa/+/+/command`
- Pubblica lo stato di ogni condizionatore su `casa/{stanza}/{cond}/telemetry` ogni 120 secondi
- Ogni 2 cicli di telemetria, la temperatura di ogni condizionatore (non in cooldown) oscilla di ±0.1°C
- Alla ricezione di un comando su `casa/{stanza}/{cond}/command`, aggiorna lo stato in memoria e attiva un cooldown di 180 secondi (la telemetria di quel condizionatore viene saltata fino a scadenza)

**Payload telemetria:**
```json
{"temperature": 23.5, "mode": "cold", "fan_speed": "auto", "status": "ON"}
```

**Payload comando (in arrivo):**
```json
{"set_temp": 20.0, "mode": "hot", "status": "OFF"}
```

**Avvio:**

```sh
uv run python publisher.py
```

### subscriber.py

Ascolta tutto il traffico MQTT su `casa/#`.

**Avvio:**

```sh
uv run python subscriber.py
```
