# MCP Server — Specifiche per l'implementazione

## Trasporto

- **URL:** `http://localhost:9100/mcp/v1`
- **Tipo:** SSE (Server-Sent Events)
- **Protocollo:** Model Context Protocol (MCP)

## Tool richiesti

Tutti i tool devono restituire `String` con dati finti. Nessuna logica reale, solo test.

### Lettura (`get_`, `read_`) — usati da `queryTool`

| Nome tool | Descrizione | Esempio risposta |
|---|---|---|
| `get_temperature` | Legge temperatura da un sensore | `"23.5°C"` |
| `get_humidity` | Legge umidità da un sensore | `"65%"` |
| `get_light_status` | Legge stato luce | `"accesa"` |
| `get_switch_status` | Legge stato interruttore | `"on"` |
| `read_sensor_status` | Legge stato generale sensore | `"online"` |
| `get_power_consumption` | Legge consumo energetico | `"150W"` |
| `read_temperature_all` | Legge temperature di tutte le zone | `"soggiorno: 22°C, camera: 20°C, cucina: 24°C"` |

### Scrittura/controllo (`set_`, `exec_`) — usati da `controllerTool`

| Nome tool | Descrizione | Esempio risposta |
|---|---|---|
| `set_light` | Accende/spegne luce | `"luce impostata a on"` |
| `set_temperature` | Imposta temperatura | `"temperatura impostata a 22°C"` |
| `set_switch` | Attiva/disattiva interruttore | `"interruttore impostato su off"` |
| `exec_command` | Esegue comando generico | `"comando eseguito con successo"` |
| `exec_reset_device` | Resetta un dispositivo | `"dispositivo resettato"` |
| `set_fan_speed` | Imposta velocità ventola | `"ventola impostata a 3"` |

### Opzionali

| Nome tool | Descrizione | Esempio risposta |
|---|---|---|
| `get_device_list` | Elenca dispositivi disponibili | `"luce_soggiorno, luce_camera, ventola, sensore_temperatura"` |
| `read_device_info` | Legge info su un dispositivo | `"modello: IoT-2024, firmware: 2.1.3, stato: online"` |

## Note implementative

- Usa il framework `quarkus-mcp-server-sse` per l'implementazione Java.
- Esponi l'SSE endpoint su `/mcp/v1`.
- I tool devono essere annotati con `@Tool` e avere una descrizione chiara.
- Tutti i dati sono finti — nessun collegamento a hardware reale.
- La porta deve essere **9100** (`quarkus.http.port=9100`).
