# InfluxDB


token adomi = apiv3_62IFXnyjthGzZQo6ikBvg0AZ5hl4pI96smizLHah9luW16HgiM2yzk0DXJiQrxFOLE3H_MvHnjf0Q9QbaaSCyA


Comando per creare database:
docker exec -it influxdb3-core influxdb3 create database prova


dati da inserire

{condizionatore="nome","temperature": 23.5, "mode": "cold", "fan_speed": "auto", "status": "ON"}


curl "http://localhost:8181/api/v3/write_lp?db=sensors&precision=auto"
  --header "Authorization: Bearer apiv3_62IFXnyjthGzZQo6ikBvg0AZ5hl4pI96smizLHah9luW16HgiM2yzk0DXJiQrxFOLE3H_MvHnjf0Q9QbaaSC"\
  --data-raw "home,room=Kitchen temp=72.0
 home,room=Living\ room temp=71.5"