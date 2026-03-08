package org.epsda.pets.config;

import org.epsda.pets.repository.MyBatisPlusChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/23
 * Time: 18:42
 *
 * @Author: 憨八嘎
 */
@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 MyBatisPlusChatMemoryRepository myBatisPlusChatMemoryRepository) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(myBatisPlusChatMemoryRepository)
                .maxMessages(10)
                .build();
        String defaultMessage = "你是小橘岛宠物商城和宠物贴吧的人工智能助手小书，" +
                "主要回答宠物和宠物用品相关的知识，如果用户问到了其他领域，则提示用户";
        return builder
                .defaultSystem(defaultMessage)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}