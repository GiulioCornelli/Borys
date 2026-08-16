package org.ai.borys.mcp.power;

import jakarta.inject.Singleton;

@Singleton
public class PowerService {

    String getConsumption() {
        return "150W";
    }
}
