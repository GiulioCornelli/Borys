package org.ai.borys.mcp.light;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LightTools {

    private final LightService lightService;

    @Inject
    LightTools(LightService lightService) {
        this.lightService = lightService;
    }

    @Tool(description = "Legge stato luce")
    String get_light_status() {
        return lightService.getStatus();
    }

    @Tool(description = "Accende/spegne luce")
    String set_light(@ToolArg(description = "Stato luce (on/off)") String value) {
        return lightService.setStatus(value);
    }
}
