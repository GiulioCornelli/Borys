import os

import uvicorn

def run_server(host: str | None = None, port: int | None = None) -> None:
    host = host or os.getenv("SERVER_HOST", "0.0.0.0")
    port = port or int(os.getenv("SERVER_PORT", "8000"))
    uvicorn.run(
        "src.app:app", 
        host=host, 
        port=port
    )
    