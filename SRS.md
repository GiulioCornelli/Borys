Agisci come un esperto Backend e IoT Engineer. Scrivi uno script Python completo utilizzando la libreria 'paho-mqtt' (con la sintassi moderna della v2.x, CallbackAPIVersion.VERSION2). 

Lo script deve simulare un pool di 10 condizionatori virtuali distribuiti in 3 stanze.

### REQUISITI DI ARCHITETTURA E NAMING:
1. Le stanze devono essere esattamente 3, nominate: 'stanza1', 'stanza2', 'stanza3'.
2. I 10 condizionatori devono essere distribuiti tra le stanze (es. 4 nella stanza1, 3 nella stanza2, 3 nella stanza3) e nominati seguendo lo schema: 'condi_stanza1_1', 'condi_stanza1_2', 'condi_stanza2_1', ecc.
3. Lo script deve mantenere in memoria uno stato iniziale per ciascuno dei 10 condizionatori. Lo stato di ognuno è un dizionario con:
   - temperature (float iniziale casuale tra 22.0 e 25.0)
   - mode (stringa, iniziale: 'cold')
   - fan_speed (stringa, iniziale: 'auto')
   - status (stringa, iniziale: 'ON')

### REQUISITI MQTT (TOPIC & PAYLOAD):
1. Lo script deve usare un UNICO client MQTT per gestire tutto il pool.
2. Deve connettersi a 'localhost' sulla porta 1883 usando le credenziali:
   - Username: 'borys_agent'
   - Password: 'MqttPasswordSegreta2026'
3. **Sottoscrizione (Commands):** All'avvio, il client deve iscriversi al topic globale: `casa/+/+/command`.
4. **Pubblicazione (Telemetry):** Ogni 120 secondi (2 minuti), lo script deve ciclare tutti e 10 i condizionatori e pubblicare il loro stato attuale sul rispettivo topic di telemetria: `casa/{nome_stanza}/{nome_condizionatore}/telemetry`.
   Il payload deve essere un JSON strutturato così (usa chiavi in snake_case):
   {"temperature": 23.5, "mode": "cold", "fan_speed": "auto", "status": "ON"}

### LOGICA ASINCRONA (GESTIONE COMANDI):
Nella callback `on_message`, lo script deve intercettare i comandi in arrivo sul topic `casa/{stanza}/{condizionatore}/command`.
1. Deve fare il parsing del topic per capire quale stanza e quale condizionatore specifico stanno ricevendo il comando.
2. Deve fare il parsing del payload JSON del comando (es. può contenere {"set_temp": 20.0, "mode": "hot", "status": "OFF"}).
3. Deve aggiornare lo stato interno in memoria di QUEL condizionatore specifico con i nuovi valori ricevuti.
4. Deve stampare un log chiaro a schermo, ad esempio: "⚙️ [COMANDO RICEVUTO] Aggiornato condi_stanza1_2 -> Temp: 20.0, Status: OFF".

### STRUTTURA DEL CODICE:
- Usa `client.loop_start()` per gestire la rete MQTT in background senza bloccare il thread principale.
- Nel loop principale (`while True`), gestisci il timer dei 2 minuti per l'invio della telemetria senza usare un semplice `time.sleep(120)` bloccante, in modo che lo script sia reattivo nel terminale se l'utente preme CTRL+C.
- Inserisci dei print di log eleganti e scannabili nel terminale per monitorare le telemetrie inviate e i comandi ricevuti.