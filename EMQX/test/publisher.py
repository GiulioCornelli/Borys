import json
import os
import random
import signal
import sys
import time
import paho.mqtt.client as mqtt

# ── Carica .env (se presente) ──
def load_env():
    env_path = os.path.join(os.path.dirname(__file__), ".env")
    if not os.path.isfile(env_path):
        return
    with open(env_path) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, val = line.partition("=")
            os.environ.setdefault(key.strip(), val.strip())

load_env()

# ── Configurazione MQTT ──
BROKER = os.getenv("MQTT_BROKER_HOST", "localhost")
PORT = int(os.getenv("MQTT_BROKER_PORT", "1883"))
MQTT_USERNAME = os.getenv("MQTT_USERNAME")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD")

# ── Pool di condizionatori virtuali ──
# 3 stanze, 10 condizionatori totali (4+3+3)
STANZE = {
    "stanza1": [f"condi_stanza1_{i}" for i in range(1, 5)],
    "stanza2": [f"condi_stanza2_{i}" for i in range(1, 4)],
    "stanza3": [f"condi_stanza3_{i}" for i in range(1, 4)],
}

# Intervallo di pubblicazione telemetria (secondi)
TELEMETRY_INTERVAL = 120
# Cooldown: dopo un comando, la telemetria di quel condizionatore viene saltata per N secondi
COMMAND_COOLDOWN = 180

# ── Stato iniziale dei condizionatori ──
cond_state = {}
last_command_time = {}
telemetry_cycle = 0
for stanza, conds in STANZE.items():
    # Stessa temperatura base per tutti i condizionatori della stanza
    base_temp = round(random.uniform(22.0, 25.0), 1)
    for cond in conds:
        cond_state[cond] = {
            "temp": base_temp,
            "mode": "cold",
            "fan_speed": "auto",
            "status": "ON",
        }
        last_command_time[cond] = 0.0

running = True


# ── Callback avvenuta connessione ──
def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code == 0:
        print("✅ Connesso al broker MQTT")
        # Sottoscrizione globale ai comandi per tutti i condizionatori
        client.subscribe("casa/+/+/command")
        print("📡 Sottoscritto a casa/+/+/command")
    else:
        print(f"❌ Connessione fallita, reason code: {reason_code}")


# ── Callback ricezione comandi ──
def on_message(client, userdata, msg):
    """
    Riceve comandi su casa/{stanza}/{cond}/command.
    Aggiorna lo stato in memoria del condizionatore target e registra il timestamp
    per attivare il cooldown di pubblicazione.
    """
    try:
        topic_parts = msg.topic.split("/")
        if len(topic_parts) < 4:
            return
        stanza = topic_parts[1]
        cond_name = topic_parts[2]
        full_name = f"condi_{stanza}_{cond_name.removeprefix('condi_')}" if not cond_name.startswith("condi_") else cond_name

        if full_name not in cond_state:
            print(f"⚠️ Condizionatore sconosciuto: {full_name}")
            return

        payload = json.loads(msg.payload.decode())
        state = cond_state[full_name]

        if "set_temp" in payload:
            state["temperature"] = float(payload["set_temp"])
        if "mode" in payload:
            state["mode"] = payload["mode"]
        if "fan_speed" in payload:
            state["fan_speed"] = payload["fan_speed"]
        if "status" in payload:
            state["status"] = payload["status"]

        # Segna l'istante del comando per il cooldown
        last_command_time[full_name] = time.monotonic()

        print(
            f"⚙️ [COMANDO RICEVUTO] Aggiornato {full_name} -> "
            f"Temp: {state['temperature']}, Mode: {state['mode']}, "
            f"Fan: {state['fan_speed']}, Status: {state['status']}"
        )
    except Exception as e:
        print(f"❌ Errore nel processare il comando: {e}")


# ── Deriva naturale della temperatura ──
def apply_drift():
    """
    Ogni 2 cicli di telemetria, fa oscillare la temperatura di ±0.1°C
    per tutti i condizionatori che non sono in cooldown.
    """
    global telemetry_cycle
    telemetry_cycle += 1
    if telemetry_cycle % 2 != 0:
        return
    for stanza, conds in STANZE.items():
        for cond in conds:
            if time.monotonic() - last_command_time[cond] < COMMAND_COOLDOWN:
                continue
            drift = random.choice([-0.1, 0.1])
            cond_state[cond]["temperature"] = round(cond_state[cond]["temperature"] + drift, 1)


# ── Pubblicazione telemetria ──
def publish_telemetry(client):
    """
    Cicla tutti i condizionatori e pubblica lo stato su
    casa/{stanza}/{cond}/telemetry saltando quelli in cooldown.
    """
    apply_drift()
    now = time.monotonic()
    for stanza, conds in STANZE.items():
        for cond in conds:
            if now - last_command_time[cond] < COMMAND_COOLDOWN:
                remaining = int(COMMAND_COOLDOWN - (now - last_command_time[cond]))
                print(f"⏳ [COOLDOWN] {cond} — salto telemetria per altri {remaining}s")
                continue
            state = cond_state[cond]
            topic = f"casa/{stanza}/{cond}/telemetry"
            payload = json.dumps(state)
            client.publish(topic, payload)
            print(f"📤 [TELEMETRIA] {topic} -> {payload}")


# ── Gestione arresto pulito ──
def signal_handler(sig, frame):
    global running
    print("\n🛑 Arresto in corso...")
    running = False


# ── Entry point ──
def main():
    global running
    signal.signal(signal.SIGINT, signal_handler)

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    # if MQTT_USERNAME and MQTT_PASSWORD:
    #     client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(BROKER, PORT, 60)
    # loop_start() gestisce la rete in background senza bloccare
    client.loop_start()

    last_telemetry = 0

    # Loop principale: timer non bloccante per telemetria
    while running:
        now = time.monotonic()
        if now - last_telemetry >= TELEMETRY_INTERVAL:
            publish_telemetry(client)
            last_telemetry = now

        time.sleep(0.5)

    client.loop_stop()
    client.disconnect()
    print("👋 Script terminato")


if __name__ == "__main__":
    main()
