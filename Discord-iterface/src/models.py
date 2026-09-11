from dataclasses import dataclass


@dataclass
class ChatRequest:
    message: str
    sessionId: str


@dataclass
class ChatResponse:
    sessionId: str
    response: str
    category: str
