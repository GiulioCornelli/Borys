package org.ai.borys.mcp;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Singleton;

@Singleton
public class BorysMcpTools {

    @Tool(description = "Legge temperatura da un sensore")
    String get_temperature() {
        return "23.5°C";
    }

    @Tool(description = "Legge umidità da un sensore")
    String get_humidity() {
        return "65%";
    }

    @Tool(description = "Legge stato luce")
    String get_light_status() {
        return "accesa";
    }

    @Tool(description = "Legge stato interruttore")
    String get_switch_status() {
        return "on";
    }

    @Tool(description = "Legge stato generale sensore")
    String read_sensor_status() {
        return "online";
    }

    @Tool(description = "Legge consumo energetico")
    String get_power_consumption() {
        return "150W";
    }

    @Tool(description = "Legge temperature di tutte le zone")
    String read_temperature_all() {
        return "soggiorno: 22°C, camera: 20°C, cucina: 24°C";
    }

    @Tool(description = "Accende/spegne luce")
    String set_light(@ToolArg(description = "Stato luce (on/off)") String value) {
        return "luce impostata a " + value;
    }

    @Tool(description = "Imposta temperatura")
    String set_temperature(@ToolArg(description = "Temperatura desiderata") String value) {
        return "temperatura impostata a " + value;
    }

    @Tool(description = "Attiva/disattiva interruttore")
    String set_switch(@ToolArg(description = "Stato interruttore (on/off)") String value) {
        return "interruttore impostato su " + value;
    }

    @Tool(description = "Esegue comando generico")
    String exec_command(@ToolArg(description = "Comando da eseguire") String command) {
        return "comando eseguito con successo";
    }

    @Tool(description = "Resetta un dispositivo")
    String exec_reset_device(@ToolArg(description = "Identificativo dispositivo") String device) {
        return "dispositivo resettato";
    }

    @Tool(description = "Imposta velocità ventola")
    String set_fan_speed(@ToolArg(description = "Velocità ventola") String speed) {
        return "ventola impostata a " + speed;
    }

    @Tool(description = "Elenca dispositivi disponibili")
    String get_device_list() {
        return "luce_soggiorno, luce_camera, ventola, sensore_temperatura";
    }

    @Tool(description = "Legge info su un dispositivo")
    String read_device_info(@ToolArg(description = "Nome del dispositivo") String device) {
        return "modello: IoT-2024, firmware: 2.1.3, stato: online";
    }
}
