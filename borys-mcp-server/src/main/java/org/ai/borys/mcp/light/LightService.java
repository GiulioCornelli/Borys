package org.ai.borys.mcp.light;

import jakarta.inject.Singleton;

@Singleton
public class LightService {

    String getStatus() {
        return "accesa";
    }

    String setStatus(String value) {
        return "luce impostata a " + value;
    }
}
