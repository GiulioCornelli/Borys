import asyncio

from dotenv import load_dotenv

from config.serverConfig import start_server
from src.bots.bot import start_bot


async def main():
    load_dotenv()
    try:
        await asyncio.gather(start_server(), start_bot())
    except asyncio.CancelledError:
        pass


if __name__ == "__main__":
    asyncio.run(main())
