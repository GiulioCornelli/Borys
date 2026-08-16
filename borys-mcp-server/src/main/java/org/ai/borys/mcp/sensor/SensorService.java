package org.ai.borys.mcp.sensor;

import jakarta.inject.Singleton;

@Singleton
public class SensorService {

    String getHumidity() {
        return "65%";
    }

    String readStatus() {
        return "online";
    }
}
