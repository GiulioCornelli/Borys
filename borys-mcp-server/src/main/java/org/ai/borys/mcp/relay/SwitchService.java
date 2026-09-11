package org.ai.borys.mcp.relay;

import jakarta.inject.Singleton;

@Singleton
public class SwitchService {

    String getStatus() {
        return "on";
    }

    String setStatus(String value) {
        return "interruttore impostato su " + value;
    }
}
