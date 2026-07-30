package com.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import jakarta.inject.Singleton;

@RegisterAiService
@Singleton
public interface ControllerAgent {

    @SystemMessage("""
        Sei un assistente specializzato nel controllo di dispositivi IoT.
        Hai a disposizione vari strumenti, ma PUOI utilizzare SOLO quelli che
        iniziano con "set_" o "exec_". Ignora tutti gli altri.
        Rispondi in modo chiaro e conciso.
    """)
    @McpToolBox("borysmcp")
    String chat(@UserMessage String message);
}
