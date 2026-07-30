package com.agents;

import com.Tools.NameTool;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.inject.Singleton;

@RegisterAiService
@Singleton
public interface RouterAgent {

    enum CategoryCustom {GETVALU, SETVALU, NAME ,GENERAL}

    @SystemMessage("""
        Sei un agente orchestratore che deve analizzare il testo in ingresso,
        ed etichettarlo con una delle seguenti etichette:
        
        - GETVALU: Se l'utente deve ottenere dei dati da dei dispositivi 
        
        - SETVALU: Se l'utente vuole modificare lo stato, un valore o altre caratteristiche di un dispositivo IoT

        - NAME: Se ti chiedono quale è il tuo nome
        
        - GENERAL: Se non è nessuna delle altre

        (Mi raccomando rispondi solo con una di queste etichette)
        
    """)

    CategoryCustom findeCategory(@UserMessage String message);
    String resolvRequest(@UserMessage String message);


    @SystemMessage("""
        Sei un assistente amichevole. Quando ti viene chiesto il tuo nome,
        usa il tool getName per rispondere.
    """)
    @ToolBox(NameTool.class)
    String sendYourName(@UserMessage String message);

}
