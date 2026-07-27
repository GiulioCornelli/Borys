import os

from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware


def create_app() -> FastAPI:
    production = os.getenv("PRODUCTION", "false").strip().lower() == "true"

    docs_url = None if production else "/docs"
    redoc_url = None if production else "/redoc"


    app = FastAPI(
        title="Discord Interface",
        docs_url=docs_url,
        redoc_url=redoc_url,
    )

    if production:
        app.add_middleware()
    else:
        app.add_middleware(CORSMiddleware, allow_origins="*")
    

    # ---------- Routes ----------

    @app.get("/health")
    def health():
        return {"status": "ok"}
    return app


app = create_app()