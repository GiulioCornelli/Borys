import logging

from discord.ext import commands

logger = logging.getLogger("borys_bot")


class CommandCog(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @commands.command(name="ping")
    async def ping(self, ctx: commands.Context):
        await ctx.send("Pong!")


async def setup(bot):
    await bot.add_cog(CommandCog(bot))
