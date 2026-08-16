import asyncio
from functools import cache
import logging
from time import sleep
import httpx

import discord
from discord.ext import commands

logger = logging.getLogger("borys_bot")


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

        if self._message_check(message) == True:
            await message.delete()
            await message.channel.send(f"{message.author.mention} - Qui non si può usare questa parola!")
            return
        
        try:
            async with httpx.AsyncClient(timeout=120.0) as client:
                resp = await client.post("http://localhost:9090/api/chat", content=message.content, headers={"Content-Type": "text/plain"})
                resp.raise_for_status()
                response_text = resp.text
                logger.info(response_text)
                await message.channel.send(response_text)
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


