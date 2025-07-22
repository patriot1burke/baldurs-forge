package org.baldurs.forge.builder;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.NotImplementedException;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.spi.CDI;

public class JsonChatMemoryProvider implements Supplier<ChatMemoryProvider> {
    private static class ChatMemoryDelegate implements ChatMemory {
        ChatMemory getDelegate() {
            try {
                // this is a hack because AIServiceContext caches ChatMemory instances and Quarkus tries to evict them when the AIService is destroyed
                // Since our chat memories are RequestScoped, then this causes an error to be thrown.
                // With this approach, only the ChatMemoryDelegate is cached and we return a dummy if there is no request scope.
                ChatMemory memory = CDI.current().select(JsonChatMemory.class).get();
                memory.id();
                return memory;
            } catch (Throwable e) {
                return new ChatMemory() {

                    @Override
                    public void add(ChatMessage message) {
                        Log.error("add Should not be called");
                        throw new NotImplementedException("Should not be called");
                        
                    }

                    @Override
                    public void clear() {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public Object id() {
                        Log.error("id Should not be called");
                        throw new NotImplementedException("Should not be called");
                    }

                    @Override
                    public List<ChatMessage> messages() {
                        Log.error("messages Should not be called");
                        throw new NotImplementedException("Should not be called");
                    }
                   
                };
            }
        }

        @Override
        public void add(ChatMessage message) {
            getDelegate().add(message);
            
        }

        @Override
        public void clear() {
            getDelegate().clear();
            
        }

        @Override
        public Object id() {
            return getDelegate().id();
        }

        @Override
        public List<ChatMessage> messages() {
            return getDelegate().messages();
        }

        

    }

    private static ChatMemoryDelegate chatMemoryDelegate = new ChatMemoryDelegate();




    @Override
    public ChatMemoryProvider get() {
        //Log.info("Getting JsonChatMemoryProvider");
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object id) {
                return chatMemoryDelegate;
            }
        }; 
    }

}
