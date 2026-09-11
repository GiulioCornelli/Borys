package org.ai.borys.mcp.device;

import jakarta.inject.Singleton;

@Singleton
public class DeviceService {

    String listDevices() {
        return "luce_soggiorno, luce_camera, ventola, sensore_temperatura";
    }

    String readInfo(String device) {
        return "modello: IoT-2024, firmware: 2.1.3, stato: online";
    }

    String execCommand(String command) {
        return "comando eseguito con successo";
    }

    String resetDevice(String device) {
        return "dispositivo resettato";
    }
}
