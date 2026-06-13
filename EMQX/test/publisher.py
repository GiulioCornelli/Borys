import random
import time
import paho.mqtt.client as mqtt

BROKER = "localhost"
PORT = 1883

CONDIZIONATORI = [
    "/casa/stanza1/cond1/",
    "/casa/stanza1/cond2/",
    "/casa/stanza2/cond1/",
    "/casa/stanza2/cond2/",
    "/casa/stanza2/cond3/",
]

def main():
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.connect(BROKER, PORT, 60)

    while True:
        for topic in CONDIZIONATORI:
            value = random.randint(19, 28)
            client.publish(topic, str(value))
            print(f"Published {value} on {topic}")
            time.sleep(0.5)
        time.sleep(200)

if __name__ == "__main__":
    main()
