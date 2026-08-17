# LangChain4j — Guida completa alla Chat Memory

> Studio di tutte le funzioni e classi che langchain4j mette a disposizione per gestire la memoria conversazionale.
> Fonte: documentazione ufficiale langchain4j + quarkus-langchain4j.

---

## 1. Il problema: i LLM sono stateless

I modelli di linguaggio non mantengono lo stato della conversazione. Ogni chiamata è indipendente. Per simulare una conversazione multi-turn, bisogna reinviare tutti i messaggi precedenti ad ogni interazione. `ChatMemory` gestisce questo meccanismo automaticamente.

---

## 2. ChatMemory — L'interfaccia base

```java
public interface ChatMemory {

    Object id();

    void add(ChatMessage message);

    void add(ChatMessage... messages);

    void add(Iterable<ChatMessage> messages);

    void set(ChatMessage... messages);

    void set(Iterable<ChatMessage> messages);

    List<ChatMessage> messages();

    void clear();
}
```

### Metodi

| Metodo | Descrizione |
|--------|-------------|
| `id()` | Restituisce l'ID univoco di questa istanza di memoria |
| `add(ChatMessage)` | Aggiunge un singolo messaggio alla storia |
| `add(ChatMessage...)` | Aggiunge più messaggi in una volta |
| `add(Iterable<ChatMessage>)` | Aggiunge una collezione di messaggi |
| `set(ChatMessage...)` | **Sostituisce** l'intera storia con i messaggi forniti (usato per compaction/riorganizzazione) |
| `set(Iterable<ChatMessage>)` | Idem, da Iterable |
| `messages()` | Restituisce i messaggi attualmente in memoria (non necessariamente tutti quelli mai aggiunti) |
| `clear()` | Svuota completamente la memoria |

**Nota su `set()`**: questo metodo NON viene chiamato automaticamente da langchain4j. È disponibile per casi avanzati come memory compaction (riassumere la storia e riscriverla).

---

## 3. ChatMemoryProvider — Factory per le istanze di memoria

```java
public interface ChatMemoryProvider {
    ChatMemory get(Object memoryId);
}
```

È l'interfaccia che langchain4j usa per ottenere l'istanza di `ChatMemory` associata a un determinato `memoryId`. Viene invocata automaticamente quando un AiService con `@MemoryId` viene chiamato con un ID nuovo.

**Flusso interno:**
1. Arriva una richiesta con `@MemoryId String sessionId = "abc-123"`
2. LangChain4j chiama `chatMemoryProvider.get("abc-123")`
3. Il provider restituisce (o crea) l'istanza `ChatMemory` per quel sessionId
4. LangChain4j aggiunge i messaggi e poi chiama il LLM con lo storico

---

## 4. Implementazioni concrete di ChatMemory

### 4.1 MessageWindowChatMemory (la più semplice)

Finestra scorrevole basata sul **numero di messaggi**. Mantiene gli ultimi N messaggi, scartando i più vecchi.

```java
ChatMemory memory = MessageWindowChatMemory.builder()
    .id("session-123")
    .maxMessages(20)          // finestra di 20 messaggi
    .chatMemoryStore(store)   // opzionale, default: InMemoryChatMemoryStore
    .build();
```

**Come funziona la sliding window:**

```
maxMessages = 4

Aggiunta 1: [SystemMessage]
Aggiunta 2: [SystemMessage, UserMessage("ciao")]
Aggiunta 3: [SystemMessage, UserMessage("ciao"), AiMessage("ciao!")]
Aggiunta 4: [SystemMessage, UserMessage("ciao"), AiMessage("ciao!"), UserMessage("temp?")]
Aggiunta 5: [SystemMessage, UserMessage("ciao"), AiMessage("ciao!"), UserMessage("temp?"), AiMessage("25°C")]
                                         ↑ SystemMessage MAI scartato    ↑ UserMessage("ciao") SCARTATO
```

**Regole importanti:**
- Il `SystemMessage` viene **sempre** mantenuto (non viene mai scartato)
- I messaggi sono indivisibili: se un messaggio non entra, viene scartato interamente
- Utile per prototipazione rapida — non considera il numero di token

**Costruttore dynamic:** Puoi passare un `IntSupplier` invece di un valore fisso per cambiare la dimensione della finestra a runtime:

```java
ChatMemory memory = MessageWindowChatMemory.builder()
    .id("session-123")
    .maxMessages(() -> dynamicMaxMessages)  // cambia a runtime
    .build();
```

### 4.2 TokenWindowChatMemory (più sofisticata)

Finestra scorrevole basata sul **numero di token**. Mantiene i messaggi più recenti che rientrano nel limite di token.

```java
ChatMemory memory = TokenWindowChatMemory.builder()
    .id("session-123")
    .maxTokens(4000, new OpenAiTokenCountEstimator())
    .chatMemoryStore(store)
    .build();
```

**Differenze con MessageWindowChatMemory:**

| Caratteristica | MessageWindow | TokenWindow |
|---------------|---------------|-------------|
| Criterio di eviction | Conta messaggi | Conta token |
| Precisione | Bassa (un messaggio lungo = tanto spazio) | Alta (rispetta il contesto del modello) |
| Dipendenza | Nessuna | Serve un `TokenCountEstimator` |
| Caso d'uso | Prototipi, demo | Produzione, modelli con contesto limitato |

**TokenCountEstimator** — Interfaccia per contare i token:

```java
public interface TokenCountEstimator {
    int estimateTokenCountInMessage(ChatMessage message);
    int estimateTokenCountInMessages(Iterable<ChatMessage> messages);
}
```

Langchain4j fornisce implementazioni predefinite per vari provider:
- `OpenAiTokenCountEstimator` (per modelli OpenAI)
- `HuggingFaceTokenCountEstimator` (usa tokenizers HuggingFace)
- Puoi implementarne una custom per il tuo modello

**Regole:**
- I messaggi sono indivisibili: se un messaggio non entra nel budget di token, viene scartato interamente
- Il `SystemMessage` viene sempre mantenuto
- Quando un messaggio viene scartato, il suo token count viene liberato per nuovi messaggi

---

## 5. ChatMemoryStore — Persistenza

### 5.1 InMemoryChatMemoryStore (default)

```java
public class InMemoryChatMemoryStore implements ChatMemoryStore {
    private final Map<Object, List<ChatMessage>> storage = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) { ... }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) { ... }

    @Override
    public void deleteMessages(Object memoryId) { ... }
}
```

- Archiviazione in un `ConcurrentHashMap` nella JVM
- Persa al riavvio dell'applicazione
- Perfetta per MVP e sviluppo

### 5.2 ChatMemoryStore — Interfaccia per persistenza custom

```java
public interface ChatMemoryStore {

    List<ChatMessage> getMessages(Object memoryId);

    void updateMessages(Object memoryId, List<ChatMessage> messages);

    void deleteMessages(Object memoryId);
}
```

**Metodi:**

| Metodo | Quando viene chiamato | Cosa fa |
|--------|----------------------|---------|
| `getMessages(memoryId)` | Ad ogni interazione col LLM (1 volta) | Carica tutti i messaggi per quel memoryId |
| `updateMessages(memoryId, messages)` | Ad ogni aggiunta di messaggio (2 volte per interazione: UserMessage + AiMessage) | Aggiorna l'intera lista dei messaggi nel punto di persistenza |
| `deleteMessages(memoryId)` | Quando viene chiamato `ChatMemory.clear()` | Elimina tutti i messaggi per quel memoryId |

**Esempio — Store con Redis:**

```java
@ApplicationScoped
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Inject
    RedisClient redis;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redis.get("chat:" + memoryId);
        if (json == null) return List.of();
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redis.set("chat:" + memoryId, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.del("chat:" + memoryId);
    }
}
```

**Esempio — Store con PostgreSQL:**

```java
@ApplicationScoped
public class PgChatMemoryStore implements ChatMemoryStore {

    @Inject
    AgroalDataSource dataSource; // Quarkus extension per JDBC

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // SELECT messages FROM chat_memory WHERE session_id = ?
        // deserialize con ChatMessageDeserializer.messagesFromJson()
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        // UPSERT INTO chat_memory (session_id, messages) VALUES (?, ?)
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // DELETE FROM chat_memory WHERE session_id = ?
    }
}
```

### 5.3 Serializzazione/Deserializzazione

Langchain4j fornisce utility per convertire messaggi in/from JSON:

```java
// Serializzazione
String json = ChatMessageSerializer.messageToJson(userMessage);
String json = ChatMessageSerializer.messagesToJson(listOfMessages);

// Deserializzazione
ChatMessage msg = ChatMessageDeserializer.messageFromJson(json);
List<ChatMessage> msgs = ChatMessageDeserializer.messagesFromJson(json);
```

Questi metodi sono utili quando implementi un `ChatMemoryStore` custom.

---

## 6. I tipi di ChatMessage

Ogni messaggio nella memoria è un'istanza di una delle sottoclassi:

```
ChatMessage (abstract)
├── SystemMessage      — Istruzioni di sistema (es. "Sei un assistente...")
├── UserMessage        — Input dell'utente
│   ├── con @UserMessage annotation
│   └── con variabili template
├── AiMessage          — Risposta del modello
│   ├── testo semplice
│   └── con tool execution request
└── ToolExecutionResultMessage — Risultato dell'esecuzione di un tool
```

**Cosa viene salvato nella memoria:**
- `SystemMessage` — SEMPRE mantenuto (mai evicted)
- `UserMessage` — Input dell'utente ad ogni turno
- `AiMessage` — Risposta del modello
- `ToolExecutionResultMessage` — Risultati delle chiamate a tool (se presenti)

**Flusso tipico di una singola interazione:**

```
1. UserMessage("quanto costa la luce?")
   → aggiunto alla memoria
2. AiMessage con ToolExecutionRequest("get_power_consumption")
   → aggiunto alla memoria
3. ToolExecutionResultMessage("Il consumo è 2.3 kW")
   → aggiunto alla memoria
4. AiMessage("Il consumo della stanza1 è di 2.3 kW")
   → aggiunto alla memoria

= 4 messaggi aggiunti in una singola interazione
```

---

## 7. @MemoryId — Isolamento per sessione/utente

```java
@RegisterAiService
@ApplicationScoped
public interface MyAgent {

    @SystemMessage("Sei un assistente intelligente")
    String chat(@MemoryId String sessionId, @UserMessage String message);
}
```

**Cosa fa:** lega un'istanza di memoria a un ID specifico. Ogni `sessionId` diverso ha la sua storia conversazionale separata.

**Come funziona internamente:**
1. Chiamata 1: `agent.chat("session-A", "ciao")` → crea/ottiene memoria per "session-A"
2. Chiamata 2: `agent.chat("session-A", "come stai?")` → usa la stessa memoria di "session-A"
3. Chiamata 3: `agent.chat("session-B", "ciao")` → crea memoria separata per "session-B"

**Regola fondamentale:** `@MemoryId` deve essere **stabile** tra le chiamate. Se cambi ID tra una richiesta e l'altra, perdi la storia.

**Tipi supportati per @MemoryId:** `String`, `Integer`, `Long`, `UUID`, qualsiasi tipo. Ma `String` è il più comune (e consigliato).

---

## 8. Integrazione con Quarkus

### 8.1 Configurazione via application.yml

```yaml
quarkus:
  langchain4j:
    chat-memory:
      type: MESSAGE_WINDOW          # oppure TOKEN_WINDOW
      memory-window:
        max-messages: 20            # finestra per MESSAGE_WINDOW
      token-window:
        max-tokens: 4000            # finestra per TOKEN_WINDOW
```

| Property | Valori | Default | Descrizione |
|----------|--------|---------|-------------|
| `type` | `MESSAGE_WINDOW`, `TOKEN_WINDOW` | `MESSAGE_WINDOW` | Tipo di finestra |
| `memory-window.max-messages` | intero > 0 | `10` | Numero max di messaggi |
| `token-window.max-tokens` | intero > 0 | `1000` | Numero max di token |

### 8.2 Scope CDI e comportamento della memoria

Il scope del bean AiService è **fondamentale** per la memoria:

| Scope | Comportamento memoria | Caso d'uso |
|-------|----------------------|------------|
| `@RequestScope` (default) | La memoria viene **cancellata** alla fine di ogni HTTP request | Prompts che NON necessitano di memoria multi-turn |
| `@ApplicationScoped` + `@MemoryId` | La memoria **persiste** tra le richieste, isolata per sessionId | Chat multi-turn (il nostro caso) |
| `@SessionScoped` (WebSocket) | La memoria persiste per tutta la durata della sessione WebSocket | Chat via WebSocket |

**ATTENZIONE**: `@ApplicationScoped` SENZA `@MemoryId` causa memory leak — ogni request crea una nuova memoria che non viene mai pulita.

### 8.3 Bean ChatMemoryProvider custom

Se vuoi controllare la creazione della memoria (es. finestra dinamica, store custom):

```java
@ApplicationScoped
public class CustomChatMemoryProvider implements ChatMemoryProvider {

    @Override
    public ChatMemory get(Object memoryId) {
        return MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(20)
            .chatMemoryStore(new InMemoryChatMemoryStore()) // o un custom store
            .build();
    }
}
```

**Regola:** se esiste un bean `ChatMemoryProvider` nel contesto CDI, Quarkus-langchain4j lo usa al posto del default.

### 8.4 ChatMemoryProviderSupplier (alternativa)

Per casi avanzati, puoi specificare un `Supplier<ChatMemoryProvider>` direttamente nell'annotazione:

```java
@RegisterAiService(chatMemoryProviderSupplier = MySupplier.class)
public interface MyAgent { ... }
```

```java
public class MySupplier implements Supplier<ChatMemoryProvider> {
    @Override
    public ChatMemoryProvider get() {
        return memoryId -> MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(15)
            .build();
    }
}
```

### 8.5 Flush strategies

Langchain4j-quarkus supporta due strategie di flush della memoria:

| Strategia | Comportamento | Vantaggio |
|-----------|---------------|-----------|
| `DEFERRED` (default) | I messaggi vengono scritti allo store **dopo** che l'invocazione del LLM ha successo | Supporta `@Retry`: se fallisce, i messaggi non vengono salvati |
| `IMMEDIATE` | I messaggi vengono scritti allo store **subito** quando aggiunti | Lo store riflette sempre lo stato attuale, anche durante l'invocazione |

Configurazione:
```yaml
quarkus:
  langchain4j:
    chat-memory:
      flush-strategy: IMMEDIATE  # o DEFERRED (default)
```

---

## 9. SeedMemory — Pre-caricare contesto

`SeedMemory` permette di pre-caricare la memoria con messaggi iniziali prima della prima interazione:

```java
@RegisterAiService
@SeedMemory(MySeedMessages.class)
public interface MyAgent { ... }
```

```java
public class MySeedMessages implements Supplier<List<ChatMessage>> {
    @Override
    public List<ChatMessage> get() {
        return List.of(
            SystemMessage.from("Sei Borys, assistente domotica."),
            UserMessage.from("Ciao, presentati!"),
            AiMessage.from("Ciao! Sono Borys, il tuo assistente per la domotica.")
        );
    }
}
```

**Utile per:** dare contesto iniziale all'agente senza doverlo mettere nel system message.

---

## 10. Come AiServices usa la memoria automaticamente

Quando usi `@RegisterAiService`, langchain4j gestisce la memoria **automaticamente**:

```
1. Arriva: agent.chat("session-123", "quanto costa la luce?")
2. ChatMemoryProvider.get("session-123") → ottiene ChatMemory
3. ChatMemory.add(UserMessage("quanto costa la luce?"))
4. ChatMemory.messages() → [SystemMessage, UserMessage("ciao"), AiMessage("..."), UserMessage("quanto costa la luce?")]
5. LLM riceve tutti i messaggi come contesto
6. LLM risponde
7. ChatMemory.add(AiMessage("Il consumo è 2.3 kW"))
8. Viene restituita la risposta
```

**Tu non devi gestire nulla** — basta annotare il parametro con `@MemoryId` e langchain4j fa tutto il resto.

---

## 11. Esempio completo e spiegato (concetto puro)

### 11.1 Panoramica del flusso

```
Client manda { "message": "quanto costa la luce?", "sessionId": "abc-123" }
    │
    ▼
Resource.chat(ChatRequest)
    │
    ├── agent.chat("abc-123", "quanto costa la luce?")
    │      │
    │      ├── LangChain4j chiama MyMemoryProvider.get("abc-123")
    │      │      → restituisce MessageWindowChatMemory(10)
    │      │
    │      ├── LangChain4j aggiunge UserMessage alla memoria
    │      │
    │      ├── LangChain4j passa TUTTI i messaggi della memoria al LLM
    │      │
    │      ├── LLM risponde (usa i tool se necessario)
    │      │
    │      ├── LangChain4j aggiunge AiMessage alla memoria
    │      │
    │      └── Restituisce la risposta
    │
    ▼
Response JSON: { "sessionId": "abc-123", "response": "...", "category": "OK" }
```

### 11.2 Step 1 — Il Provider (factory della memoria)

```java
@ApplicationScoped
public class MyMemoryProvider implements ChatMemoryProvider {
    @Override
    public ChatMemory get(Object memoryId) {
        return MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(10)
            .build();
    }
}
```

**Cosa fa:** è una factory. Ogni volta che un agente con `@MemoryId` viene chiamato con un nuovo ID, langchain4j invoca `get()` qui e riceve una memoria vuota da 10 messaggi.

**Perché `@ApplicationScoped`:** il bean vive per tutta la durata dell'applicazione. La memoria non viene cancellata alla fine di ogni HTTP request (come succederebbe con `@RequestScoped`).

**Perché non devi iniettarla da nessuna parte:** CDI la scopre automaticamente perché è annotata con `@ApplicationScoped`. LangChain4j la trova nel contesto e la usa per tutti gli agenti che hanno `@MemoryId`.

### 11.3 Step 2 — L'Agente (usa la memoria)

```java
@RegisterAiService
@Singleton
public interface MyAgent {

    // Questo metodo HA memoria — serve per conversazioni multi-turn
    String chat(@MemoryId String sessionId, @UserMessage String message);
    //            ^^^^^^^^                ^^^^^^^^^^^^
    //            chiave della memoria     messaggio dell'utente
}
```

**Perché `@MemoryId`:** è l'annotazione che dice a langchain4j "questo parametro è la chiave della memoria". Senza di essa, ogni chiamata sarebbe stateless.

**Quando metterlo e quando no:**
- Metti `@MemoryId` ai metodi che rispondono all'utente e devono ricordare il contesto (es. `chat()`, `resolvRequest()`)
- NON metterlo ai metodi che classificano o fanno una sola operazione (es. `findeCategory()`, `sendYourName()`)

### 11.4 Step 3 — Le DTO (richiesta e risposta)

```java
public record ChatRequest(String message, String sessionId) {}
public record ChatResponse(String sessionId, String response, String category) {}
```

**Perché record e non classe:** i DTO sono container di dati immutabili. Il record genera automaticamente costruttore, getter, equals, hashCode, toString. Zero boilerplate.

### 11.5 Step 4 — L'Endpoint (collega tutto)

```java
@Path("/api/")
public class ChatResource {

    @Inject MyAgent agent;

    @POST
    @Path("chat")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response chat(ChatRequest request) {

        String userId = request.sessionId() != null
            ? request.sessionId()
            : UUID.randomUUID().toString();

        String response = agent.chat(userId, request.message());

        return Response.ok(new ChatResponse(userId, response, "OK")).build();
    }
}
```

**Punti chiave:**
- `userId` è la chiave della memoria — ogni utente ha la sua storia separata
- `agent.chat(userId, message)` — il primo parametro è il `@MemoryId`, langchain4j lo usa per trovare la memoria giusta
- Se il client non manda `sessionId`, ne generiamo uno con UUID

### 11.6 Come funziona la memoria nella pratica

**Chiamata 1:**
```json
POST /api/chat
{ "message": "ciao, presentati!", "sessionId": "user-123" }
```
1. `agent.chat("user-123", "ciao, presentati!")`
2. LangChain4j crea memoria per "user-123"
3. Memoria: `[UserMessage("ciao, presentati!")]`
4. LLM risponde: "Ciao! Sono un assistente AI."
5. Memoria: `[UserMessage("ciao, presentati!"), AiMessage("Ciao! Sono un assistente AI.")]`

**Chiamata 2 (stesso utente):**
```json
POST /api/chat
{ "message": "e dimmi di più", "sessionId": "user-123" }
```
1. `agent.chat("user-123", "e dimmi di più")`
2. LangChain4j trova la memoria di "user-123"
3. Memoria: `[UserMessage("ciao, presentati!"), AiMessage("Ciao! Sono un assistente AI."), UserMessage("e dimmi di più")]`
4. LLM vede TUTTO il contesto e risponde in modo coerente

**Chiamata 3 (utente diverso):**
```json
POST /api/chat
{ "message": "ciao!", "sessionId": "user-456" }
```
1. LangChain4j crea memoria separata per "user-456"
2. Il LLM non vede nulla della conversazione di "user-123"

### 11.7 Riepilogo componenti

| Componente | Cosa fa |
|------------|---------|
| `MyMemoryProvider` | Factory: crea una `MessageWindowChatMemory` da N messaggi per ogni sessionId |
| `@MemoryId` | Dice a langchain4j di usare la memoria e con quale chiave |
| `ChatRequest` | DTO: contiene `message` e `sessionId` dal client |
| `ChatResponse` | DTO: contiene `sessionId`, `response` e `category` per il client |
| `ChatResource` | Riceve la richiesta, passa il sessionId all'agente, restituisce JSON |

---

## 12. Link alle guide ufficiali

### LangChain4j (core)
| Risorsa | Link |
|---------|------|
| Chat Memory — Tutorial | https://docs.langchain4j.dev/tutorials/chat-memory/ |
| ChatMemory — Javadoc | https://docs.langchain4j.dev/apidocs/dev/langchain4j/memory/ChatMemory.html |
| MessageWindowChatMemory — Javadoc | https://docs.langchain4j.dev/apidocs/dev/langchain4j/memory/chat/MessageWindowChatMemory.html |
| TokenWindowChatMemory — Javadoc | https://docs.langchain4j.dev/apidocs/dev/langchain4j/memory/chat/TokenWindowChatMemory.html |
| ChatMemoryProvider — Javadoc | https://docs.langchain4j.dev/apidocs/dev/langchain4j/memory/chat/ChatMemoryProvider.html |
| ChatMemoryStore — Javadoc | https://docs.langchain4j.dev/apidocs/dev/langchain4j/memory/ChatMemoryStore.html |
| AI Services — Tutorial | https://docs.langchain4j.dev/tutorials/ai-services/ |
| @MemoryId e multi-session | https://docs.langchain4j.dev/tutorials/ai-services#memoryid |
| Fonte su GitHub (chat-memory.md) | https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/chat-memory.md |

### Quarkus LangChain4j
| Risorsa | Link |
|---------|------|
| Messages and Memory (Quarkus) | https://docs.quarkiverse.io/quarkus-langchain4j/dev/messages-and-memory.html |
| AI Services Reference (Quarkus) | https://docs.quarkiverse.io/quarkus-langchain4j/dev/ai-services.html |
| Chat Memory config | https://docs.quarkiverse.io/quarkus-langchain4j/dev/messages-and-memory.html#_chat_memory_management |
| @MemoryId in Quarkus | https://docs.quarkiverse.io/quarkus-langchain4j/dev/messages-and-memory.html#_memoryid_multi_session_memory |
| @RegisterAiService Javadoc | https://github.com/quarkiverse/quarkus-langchain4j/blob/main/core/runtime/src/main/java/io/quarkiverse/langchain4j/RegisterAiService.java |

### Articoli utili
| Risorsa | Link |
|---------|------|
| Managing Chat Memory in Quarkus Langchain4j (Bill Burke) | https://bill.burkecentral.com/2025/11/25/managing-chat-memory-in-quarkus-langchain4j/ |
| Quarkus Langchain4j Chat Memory Budget | https://www.the-main-thread.com/p/quarkus-langchain4j-chat-memory-budget |

---

*Ultimo aggiornamento: 17/08/2026*
