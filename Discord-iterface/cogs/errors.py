import logging

from discord.ext import commands

logger = logging.getLogger("borys_bot")


class ErrorsCog(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_command_error(self, ctx: commands.Context, error: commands.CommandError):
        logger.error("Errore comando %s: %s", ctx.command, error)
        await ctx.send(f"Errore: {error}")


async def setup(bot):
    await bot.add_cog(ErrorsCog(bot))
