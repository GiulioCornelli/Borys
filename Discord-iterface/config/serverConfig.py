import os

import uvicorn


async def start_server(host: str | None = None, port: int | None = None) -> None:
    host = host or os.getenv("SERVER_HOST")
    port = port or int(os.getenv("SERVER_PORT"))
    config = uvicorn.Config("src.app:app", host=host, port=port)
    server = uvicorn.Server(config)
    await server.serve()