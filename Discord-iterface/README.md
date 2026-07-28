# Discord Interface

Servizio backend che espone un'API FastAPI e gestisce un bot Discord per l'interfacciamento tra la piattaforma Discord e il resto del sistema.

## Struttura del progetto

```
discord-interface/
├── config/
│   └── serverConfig.py      # Configurazione e avvio server FastAPI
├── src/
│   ├── app.py               # Istanza FastAPI (create_app)
│   └── bots/
│       ├── bot.py            # Bot Discord (BorysBot, start_bot)
│       └── cogs/             # Moduli estensione del bot (Cogs)
│           ├── __init__.py
│           ├── command.py    # Comandi Discord (!ping, ...)
│           ├── errors.py     # Gestione errori dei comandi
│           └── events.py     # Event listeners (on_ready, ...)
├── main.py                  # Entry point (avvia server + bot)
├── pyproject.toml            # Dipendenze e metadati
└── .env                     # Variabili d'ambiente (gitignorato)
```

## Cosa sono i Cog

I Cog sono moduli che organizzano funzionalita del bot in classi separabili. Ogni Cog raggruppa comandi ed event listener correlati (es. `EventsCog` per gli eventi lifecycle, `CommandCog` per i comandi utente). Vengono caricati automaticamente all'avvio tramite `load_extension()`.

## Collegamenti

- [discord.py](https://github.com/Rapptz/discord.py)
- [FastAPI](https://fastapi.tiangolo.com/)

## Avvio

```bash
# Installa dipendenze
uv sync

# Avvia server + bot
uv run main.py
```
