package org.ai.borys.mcp.relay;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SwitchTools {

    private final SwitchService switchService;

    @Inject
    SwitchTools(SwitchService switchService) {
        this.switchService = switchService;
    }

    @Tool(description = "Legge stato interruttore")
    String get_switch_status() {
        return switchService.getStatus();
    }

    @Tool(description = "Attiva/disattiva interruttore")
    String set_switch(@ToolArg(description = "Stato interruttore (on/off)") String value) {
        return switchService.setStatus(value);
    }
}
