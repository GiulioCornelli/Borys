import logging
import os

import discord
from discord.ext import commands

logger = logging.getLogger("borys_bot")


class BorysBot(commands.Bot):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.members = True
        super().__init__(command_prefix="!", intents=intents)

    async def setup_hook(self):
        extensions = ["cogs.events", "cogs.errors", "cogs.command"]
        for ext in extensions:
            await self.load_extension(ext)
            logger.info("Modulo caricato: %s", ext)



async def start_bot():
    token = os.getenv("BORYS_TOKEN", "")
    if not token:
        logger.error("ERRORE: BORYS_TOKEN non impostato nel .env")
        return

    _setup_logging()
    bot = BorysBot()
    await bot.start(token)


def _setup_logging():
    _handler = logging.StreamHandler()
    _handler.setFormatter(
        logging.Formatter(
            "%(asctime)s [%(levelname)-8s] %(name)s: %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S",
        )
    )
    logger.addHandler(_handler)
    logger.setLevel(logging.INFO)