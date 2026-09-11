package com.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.enterprise.context.ApplicationScoped;


// Classe factory - crea la memoria conversazionale per ogni sessione
@ApplicationScoped
public class SessChatMemProvider implements ChatMemoryProvider{

    @Override
    public ChatMemory get(Object memoryId) {
        return MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(20)
            .build();
    }

}
