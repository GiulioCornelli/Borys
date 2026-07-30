package com.Tools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class McpFilter {

    @Inject McpToolProvider mcpToolProvider;

    @Produces
    @Named("queryTool")
    public ToolProvider queryToolProvider() {
        return new ToolProvider() {

            @Override
            public ToolProviderResult provideTools(ToolProviderRequest request) {
                // 1. Ottiene TUTTI i tool esposti dall'MCP server (nessun filtro)
                ToolProviderResult allTools = mcpToolProvider.provideTools(request);

                // 2. Filtra la mappa: entrySet() → stream → filter()
                //    Ogni entry ha: key = ToolSpecification (nome, descr., parametri),
                //    value = ToolExecutor (la logica che esegue il tool)
                Map<ToolSpecification, ToolExecutor> filtered = allTools.tools().entrySet().stream()
                        .filter(entry -> {
                            // 2a. Legge il nome del tool dalla specification
                            String name = entry.getKey().name();
                            // 2b. Tiene SOLO tool il cui nome inizia con "get_" o "read_"
                            return name.startsWith("get_") || name.startsWith("read_");
                        })
                        // 2c. Raccoglie il risultato in una nuova mappa
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // 3. Costruisce un nuovo ToolProviderResult con soli i tool filtrati
                return ToolProviderResult.builder()
                        .addAll(filtered)      // aggiunge tutti i tool della mappa filtrata
                        .build();              // costruisce l'oggetto immutabile
            }
        };
    }

    @Produces
    @Named("controllerTool")
    public ToolProvider controllerToolProvider() {
        return new ToolProvider() {

            @Override
            public ToolProviderResult provideTools(ToolProviderRequest request) {
                ToolProviderResult allTools = mcpToolProvider.provideTools(request);

                Map<ToolSpecification, ToolExecutor> filtered = allTools.tools().entrySet().stream()
                        .filter(entry -> {
                            String name = entry.getKey().name();
                            return name.startsWith("set_") || name.startsWith("exec_");
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                return ToolProviderResult.builder()
                        .addAll(filtered)
                        .build();
            }
        };
    }

}
