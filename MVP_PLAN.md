# Piano MVP — borys-ai-agent

> Obiettivo: MVP **completo lato agente AI**, con focus sulla **conversazione**.
> Il backend (MCP server / EMQX) resta **grezzo, con dati finti**: nessuna integrazione dati reali.

## Stato attuale

Il cuore funziona già: `POST /api/chat` (text/plain) → `RouterAgent` classifica in `GETVALU`/`SETVALU`/`NAME`/`GENERAL` → `QueryAgent`/`ControllerAgent` (con tool MCP) o `RouterAgent` stesso. Il bot Discord la chiama già e risponde.

## Cosa manca (gap analysis)

**Funzionalità core dell'agente:**
1. **Memoria conversazionale** — ogni richiesta è stateless (`RestController.java`), niente contesto multi-turn.
2. **API JSON strutturata** — `chat(String)` in text/plain non ha `sessionId` né metadati.
3. **Streaming** — il bot attende fino a 120s bloccato; manca feedback incrementale (SSE).
4. **Error handling** — con Ollama/MCP giù o timeout si ottiene una 500 non gestita; manca un fallback friendly.
5. **Affidabilità del router** — la classificazione con `qwen2.5:3b` su un solo prompt senza few-shot è fragile.

**Qualità prodotto:**
6. **Test** — non esistono test sources; servono almeno unit test sul routing e integration test dell'endpoint.
7. **Config esternalizzata** — `application.yml` hardcoda `localhost:11434` e `localhost:9100`.
8. **README** — è il boilerplate Quarkus di default; non documenta architettura, API e flusso.

**Demo:**
9. **Mini UI web** — pagina statica di chat servita da Quarkus per dimostrare la conversazione dal browser senza avviare Discord.

Piccole cose: typo `messasge` nel `RestController`, package `com` invece di `org.ai.borys`, endpoint senza validazione.

## Piano di intervento

### 1. API JSON strutturata + compatibilità
- DTO request `{ "message": string, "sessionId": string? }` → response `{ "sessionId", "response", "category" }`.
- Nuovi endpoint:
  - `POST /api/chat/json` — JSON, con sessionId.
  - `POST /api/chat/stream` — JSON → **SSE** (eventi `token` / `complete` / `error`).
- `POST /api/chat` text/plain resta **invariato** come alias deprecato (sessione effimera): il bot Discord continua a funzionare senza modifiche.
- Nel `RestController`: fix del typo `messasge` → `message` e generazione UUID quando `sessionId` è assente.

### 2. Memoria conversazionale multi-turn (cuore della demo)
- Nuovo bean `ChatMemoryProvider` (`ApplicationScoped`): `ConcurrentHashMap<sessionId, MessageWindowChatMemory>` con finestra di ~20 messaggi.
- Cleanup TTL: eviction dei `sessionId` inattivi da oltre ~30 min (scheduled job) + cap sul numero di sessioni.
- `@MemoryId String sessionId` aggiunto a `QueryAgent.chat` e `ControllerAgent.chat` (standard langchain4j). Il `RouterAgent` resta stateless.
- Effetto demo: "leggi temperatura → spegni il condizionatore → torna indietro e rileggi" funziona con contesto.

### 3. Streaming (SSE) per la conversazione
- I metodi dei due agenti tornano `TokenStream` (`@RegisterAiService` lo supporta).
- Bridge `TokenStream` → Mutiny `Multi<...>` con `StreamingResponseHandler` per l'endpoint SSE. Eventi: `onToken` (parziale), `onComplete` (risposta finale + category), `onError`.
- Grande "wow" in demo: risposte che arrivano pezzo a pezzo.

### 4. Error handling robusto
- `ExceptionMapper` globale → risposta JSON coerente `{ "error": "...", "suggestion": "..." }` in italiano.
- Cattura distinta di: Ollama down, MCP server down, timeout (messaggi friendly, non 500 brutali).
- Health check custom (opzionale) per Ollama e MCP.

### 5. Router più affidabile
- Few-shot examples nel system message di `RouterAgent`, es.:
  - "quanto consuma la stanza1?" → `GETVALU`
  - "alza la temperatura a 22" → `SETVALU`
  - "come ti chiami?" → `NAME`
  - "raccontami una barzelletta" → `GENERAL`
- Il `qwen2.5:3b` classifica meglio con esempi.

### 6. Config esternalizzata
- `application.yml` con placeholder env:
  - `${BORYS_OLLAMA_BASE_URL:http://localhost:11434}`
  - `${BORYS_MCP_URL:http://localhost:9100/mcp/v1}`
  - `${BORYS_MODEL:qwen2.5:3b}`
  - dimensione finestra memoria, timeout.

### 7. Test
- Dipendenze test: aggiungere `quarkus-junit5-mockito`.
- Integration test del `RestController` con agenti mockati: verifica routing per categoria, generazione `sessionId`, propagazione dello stesso `sessionId`.
- Unit test del `ChatMemoryProvider` (isolamento per `sessionId`, eviction).

### 8. Mini UI web (demo conversazione)
- `index.html` statico in `src/main/resources/META-INF/resources/` (servito da Quarkus su `/`).
- Chat semplice con `fetch` + SSE reader su `/api/chat/stream`, `sessionId` in `localStorage`.
- La demo "conversazione" si fa dal browser senza avviare Discord.

### 9. README
- Riscrittura: architettura, contratto API (esempi curl JSON + SSE), ordine di avvio dello stack, limitazioni note (dati MCP finti).

## Ordine di esecuzione

1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9

## Fuori scope (confermato)

- Nessuna modifica a MCP server / EMQX / Discord: restano dati finti e backend grezzo.
- Il MCP server continuerà a restituire dati finti.

## Note da validare

- **UI web**: includere la pagina statica di chat oppure dimostrare solo via curl/Discord?
- **Package `com` → `org.ai.borys`**: sistemarlo ora (refactor piccolo) o lasciarlo per non rischiare?
