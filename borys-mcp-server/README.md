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

### Esempio: il package `light`

```java
// LightService.java — logica di business
@Singleton
public class LightService {

    String getStatus() {
        return "accesa";
    }

    String setStatus(String value) {
        return "luce impostata a " + value;
    }
}
```

```java
// LightTools.java — esposizione del tool
@Singleton
public class LightTools {

    private final LightService lightService;

    @Inject
    LightTools(LightService lightService) {
        this.lightService = lightService;
    }

    @Tool(description = "Legge stato luce")
    String get_light_status() {
        return lightService.getStatus();
    }

    @Tool(description = "Accende/spegne luce")
    String set_light(@ToolArg(description = "Stato luce (on/off)") String value) {
        return lightService.setStatus(value);
    }
}
```

Questa separazione permette di testare la logica di business in isolamento e di evolvere l'integrazione con il backend (es. broker MQTT) senza modificare le firme dei tool esposti.

## Tool disponibili

### Lettura (`get_`, `read_`) — usati dall'agente per le query

| Nome tool | Descrizione |
|---|---|
| `get_temperature` | Legge temperatura da un sensore |
| `get_humidity` | Legge umidità da un sensore |
| `get_light_status` | Legge stato luce |
| `get_switch_status` | Legge stato interruttore |
| `read_sensor_status` | Legge stato generale sensore |
| `get_power_consumption` | Legge consumo energetico |
| `read_temperature_all` | Legge temperature di tutte le zone |

### Controllo (`set_`, `exec_`) — usati dall'agente per i comandi

| Nome tool | Descrizione |
|---|---|
| `set_light` | Accende/spegne luce |
| `set_temperature` | Imposta temperatura |
| `set_switch` | Attiva/disattiva interruttore |
| `exec_command` | Esegue comando generico |
| `exec_reset_device` | Resetta un dispositivo |
| `set_fan_speed` | Imposta velocità ventola |

### Opzionali

| Nome tool | Descrizione |
|---|---|
| `get_device_list` | Elenca dispositivi disponibili |
| `read_device_info` | Legge info su un dispositivo |

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
