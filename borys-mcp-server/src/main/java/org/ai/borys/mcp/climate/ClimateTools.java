package org.ai.borys.mcp.climate;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClimateTools {

    private final ClimateService climateService;

    @Inject
    ClimateTools(ClimateService climateService) {
        this.climateService = climateService;
    }

    @Tool(description = "Legge temperatura da un sensore")
    String get_temperature() {
        return climateService.getTemperature();
    }

    @Tool(description = "Imposta temperatura")
    String set_temperature(@ToolArg(description = "Temperatura desiderata") String value) {
        return climateService.setTemperature(value);
    }

    @Tool(description = "Imposta velocità ventola")
    String set_fan_speed(@ToolArg(description = "Velocità ventola") String speed) {
        return climateService.setFanSpeed(speed);
    }

    @Tool(description = "Legge temperature di tutte le zone")
    String read_temperature_all() {
        return climateService.readTemperatureAll();
    }
}
