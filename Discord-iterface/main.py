import asyncio
import threading

from dotenv import load_dotenv

from config.serverConfig import start_server
from src.bots.bot import Run_Bot


def main():
    load_dotenv()
    server_thread = threading.Thread(
        target=lambda: asyncio.run(start_server()),
        daemon=True,
    )
    server_thread.start()
    Run_Bot()


if __name__ == "__main__":
    main()
