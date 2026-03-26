package com.np.redisspring.chat.config;

import com.np.redisspring.chat.ChatRoomService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class ChatRoomSocketConfig {

    private final ChatRoomService chatRoomService;

    public ChatRoomSocketConfig(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Bean
    private HandlerMapping handlerMapping() {
        Map<String, ChatRoomService> map = Map.of("/chat", chatRoomService);
        return new SimpleUrlHandlerMapping(map, -1); // lower the priority, higher precedence
    }
}
