package org.ai.borys.mcp.sensor;

import io.quarkiverse.mcp.server.Tool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SensorTools {

    private final SensorService sensorService;

    @Inject
    SensorTools(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Tool(description = "Legge umidità da un sensore")
    String get_humidity() {
        return sensorService.getHumidity();
    }

    @Tool(description = "Legge stato generale sensore")
    String read_sensor_status() {
        return sensorService.readStatus();
    }
}
