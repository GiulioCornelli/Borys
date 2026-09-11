package org.ai.borys.mcp.climate;

import jakarta.inject.Singleton;

@Singleton
public class ClimateService {

    String getTemperature() {
        return "23.5°C";
    }

    String setTemperature(String value) {
        return "temperatura impostata a " + value;
    }

    String setFanSpeed(String speed) {
        return "ventola impostata a " + speed;
    }

    String readTemperatureAll() {
        return "soggiorno: 22°C, camera: 20°C, cucina: 24°C";
    }
}
