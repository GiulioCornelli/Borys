import paho.mqtt.client as mqtt

BROKER = "localhost"
PORT = 1883
TOPIC = "casa/#"

def on_connect(client, userdata, flags, reason_code, properties):
    print(f"Connected with reason code {reason_code}")
    client.subscribe(TOPIC)
    print(f"Subscribed to {TOPIC}")

def on_message(client, userdata, msg):
    print(f"Received on {msg.topic}: {msg.payload.decode()}")

def main():
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(BROKER, PORT, 60)
    print(f"Subscriber connecting to {BROKER}:{PORT}...")
    client.loop_forever()

if __name__ == "__main__":
    main()
