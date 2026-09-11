import logging
from dataclasses import asdict

import httpx

from src.models import ChatRequest, ChatResponse

logger = logging.getLogger("borys_bot")

DEFAULT_BASE_URL = "http://localhost:9090"
DEFAULT_TIMEOUT = 120.0


class BorysApiClient:

    def __init__(self, base_url: str = DEFAULT_BASE_URL, timeout: float = DEFAULT_TIMEOUT):
        self.base_url = base_url
        self.timeout = timeout

    async def chat(self, message: str, user_id: str) -> ChatResponse:
        request = ChatRequest(message=message, sessionId=user_id)
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            resp = await client.post(
                f"{self.base_url}/api/chat",
                json=asdict(request),
                headers={"Content-Type": "application/json"},
            )
            resp.raise_for_status()
            data = resp.json()
            return ChatResponse(
                sessionId=data["sessionId"],
                response=data["response"],
                category=data["category"],
            )
