import logging
import discord
from discord.ext import commands

logger = logging.getLogger("borys_bot")

class EventsCog(commands.Cog):

    def __init__(self, bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_ready(self):
        print("metodo settato con successo")
        logger.info(f"{self.bot.user.name} was start!")

    async def setup(bot):
        await bot.add_cog(EventsCog(bot))
        
