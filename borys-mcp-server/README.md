# borys-mcp-server

Server MCP (Model Context Protocol) basato su Quarkus. Espone come tool MCP i dispositivi IoT di casa Borys (luci, interruttori, climatizzazione, sensori, energia) affinché possano essere interrogati e comandati dall'agente AI (`borys-ai-agent`).

Il server implementa il trasporto **Streamable HTTP** su:

```
http://localhost:9100/mcp/v1
```

## Struttura del progetto

```
src/main/java/org/ai/borys/mcp/
├── climate/   → temperatura, ventola e zone
├── device/    → gestione generica dei dispositivi
├── light/     → luci
├── power/     → consumi energetici
├── relay/     → interruttori (il package si chiama "relay" perché "switch" è una parola riservata in Java)
└── sensor/    → sensori
```

Ogni package contiene una coppia di classi, una per ogni dispositivo IoT:

- **`XxxTools`** — vera esposizione dei tool. Contiene i metodi annotati con `@Tool`, i parametri con `@ToolArg` e le descrizioni in italiano che l'agente AI vede. È un sottile adattatore verso il protocollo MCP.
- **`XxxService`** — vera logica di business. Contiene l'implementazione concreta (lettura dati, invio comandi, ecc.).

Questa separazione permette di testare la logica di business in isolamento e di evolvere l'integrazione con il backend (es. broker MQTT) senza modificare le firme dei tool esposti.

## Tool disponibili

### Luce (`light`)

| Nome tool | Descrizione |
|---|---|
| `get_light_status` | Legge stato luce |
| `set_light` | Accende/spegne luce |

### Interruttori (`relay`)

| Nome tool | Descrizione |
|---|---|
| `get_switch_status` | Legge stato interruttore |
| `set_switch` | Attiva/disattiva interruttore |

### Climatizzazione (`climate`)

| Nome tool | Descrizione |
|---|---|
| `get_temperature` | Legge temperatura da un sensore |
| `set_temperature` | Imposta temperatura |
| `set_fan_speed` | Imposta velocità ventola |
| `read_temperature_all` | Legge temperature di tutte le zone |

### Sensori (`sensor`)

| Nome tool | Descrizione |
|---|---|
| `get_humidity` | Legge umidità da un sensore |
| `read_sensor_status` | Legge stato generale sensore |

### Energia (`power`)

| Nome tool | Descrizione |
|---|---|
| `get_power_consumption` | Legge consumo energetico |

### Gestione dispositivi (`device`)

| Nome tool | Descrizione |
|---|---|
| `get_device_list` | Elenca dispositivi disponibili |
| `read_device_info` | Legge info su un dispositivo |
| `exec_command` | Esegue comando generico |
| `exec_reset_device` | Resetta un dispositivo |

## Configurazione

La configurazione è in `src/main/resources/application.yml`:

```yaml
quarkus:
  http:
    port: 9100
    cors:
      enabled: true
  mcp:
    server:
      http:
        root-path: /mcp/v1
```

- **Porta:** `9100`
- **Endpoint:** `/mcp/v1` (streamable HTTP)
- **CORS:** abilitato (per il consumo da client esterni)

## Avvio in modalità test (dev)

Tramite la Quarkus CLI, modalità sviluppo con live reload:

```shell
quarkus dev
```

Il server risponde su `http://localhost:9100/mcp/v1`. Il Dev UI di Quarkus è disponibile solo in questa modalità.

## Avvio in modalità produzione

Tramite la Quarkus CLI, compila il progetto:

```shell
quarkus build
```

Viene prodotto `target/quarkus-app/quarkus-run.jar`. Avvia il server senza la modalità dev:

```shell
quarkus run
```

## Verifica

Esercita l'endpoint MCP, ad esempio con un client MCP configurato su `http://localhost:9100/mcp/v1`, oppure verifica la compilazione con `quarkus build`.
