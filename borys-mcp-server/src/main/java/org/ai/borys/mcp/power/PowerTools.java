package org.ai.borys.mcp.power;

import io.quarkiverse.mcp.server.Tool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PowerTools {

    private final PowerService powerService;

    @Inject
    PowerTools(PowerService powerService) {
        this.powerService = powerService;
    }

    @Tool(description = "Legge consumo energetico")
    String get_power_consumption() {
        return powerService.getConsumption();
    }
}
