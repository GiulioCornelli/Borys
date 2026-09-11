package org.ai.borys.mcp.device;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DeviceTools {

    private final DeviceService deviceService;

    @Inject
    DeviceTools(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Tool(description = "Elenca dispositivi disponibili")
    String get_device_list() {
        return deviceService.listDevices();
    }

    @Tool(description = "Legge info su un dispositivo")
    String read_device_info(@ToolArg(description = "Nome del dispositivo") String device) {
        return deviceService.readInfo(device);
    }

    @Tool(description = "Esegue comando generico")
    String exec_command(@ToolArg(description = "Comando da eseguire") String command) {
        return deviceService.execCommand(command);
    }

    @Tool(description = "Resetta un dispositivo")
    String exec_reset_device(@ToolArg(description = "Identificativo dispositivo") String device) {
        return deviceService.resetDevice(device);
    }
}
