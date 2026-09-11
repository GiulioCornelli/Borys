package com.Tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NameTool {

    @Tool("Permette di rispondere alle domande dove viene chiesto il tuo nome")
    public String getName(){
        return "Il mio nome è Borys, e sono il tuo assistene per la domotica, come posso aiutarti?";
    }
}
