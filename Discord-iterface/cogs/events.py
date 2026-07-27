import logging

from discord.ext import commands

logger = logging.getLogger("borys_bot")


class EventsCog(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_ready(self):
        logger.info("%s avviato!", self.bot.user.name)


async def setup(bot):
    await bot.add_cog(EventsCog(bot))
