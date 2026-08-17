import logging

import httpx

import discord
from discord.ext import commands

from src.api_client import BorysApiClient

logger = logging.getLogger("borys_bot")

api_client = BorysApiClient()


class EventsCog(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_ready(self):
        logger.info(f"{self.bot.user.name} was start!")

    @commands.Cog.listener()
    async def on_member_join(self, member):
        await member.send(f"Benvenuto nel server, {member.name}!")

    @commands.Cog.listener("on_message")
    async def prova(self, message):
        if message.author == self.bot.user:
            return

        if self._message_check(message):
            await message.delete()
            await message.channel.send(f"{message.author.mention} - Qui non si può usare questa parola!")
            return

        try:
            chat_response = await api_client.chat(message.content, str(message.author.id))
            logger.info(chat_response.response)
            await message.channel.send(chat_response.response)
        except httpx.ConnectError:
            logger.error("Errore di connessione")
            await message.channel.send("Ci dispiace c'è statu un errore, risolveremo al più presto.")
        except httpx.ReadTimeout:
            logger.error("L'agente ci ha messo troppo")
            await message.channel.send("Ci dispiace c'è statu un errore, risolveremo al più presto.")

    def _message_check(self, message) -> bool:
        logger.info(f"Contenuto letto da _message_check: '{message.content}'")
        return "shit" in message.content.lower()


async def setup(bot):
    await bot.add_cog(EventsCog(bot))


