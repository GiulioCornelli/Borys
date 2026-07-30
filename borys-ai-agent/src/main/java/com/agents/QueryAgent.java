package com.agents;

import java.util.function.Supplier;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolProvider;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@RegisterAiService(toolProviderSupplier = QueryAgent.QueryToolSupplier.class)
@Singleton
public interface QueryAgent {

    @SystemMessage("""
        Sei un assistente specializzato nella lettura di dati da dispositivi IoT.
        Hai a disposizione strumenti per ottenere dati (get_, read_).
        Rispondi in modo chiaro e conciso.
    """)
    String chat(@UserMessage String message);

    @Singleton
    class QueryToolSupplier implements Supplier<ToolProvider> {

        @Inject
        @Named("queryTool")
        ToolProvider queryTool;

        @Override
        public ToolProvider get() {
            return queryTool;
        }
    }
}
