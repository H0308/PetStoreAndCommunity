package org.epsda.pets.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.AiChatMemoryMapper;
import org.epsda.pets.mapper.AiChatMetadataMapper;
import org.epsda.pets.pojo.AiChatMemory;
import org.epsda.pets.pojo.AiChatMetadata;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/03/03
 * Time: 20:53
 *
 * @Author: 憨八嘎
 */
@Repository
public class MyBatisPlusChatMemoryRepository implements ChatMemoryRepository {
    @Autowired
    private AiChatMemoryMapper aiChatMemoryMapper;
    @Autowired
    private AiChatMetadataMapper aiChatMetadataMapper;

    @Override
    public List<String> findConversationIds() {
        List<AiChatMemory> chatMemories = aiChatMemoryMapper.selectList(null);
        return chatMemories.stream().map(AiChatMemory::getConversationId).toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 根据conversationId查询历史消息
        List<AiChatMemory> chatMemories = aiChatMemoryMapper.selectAllByConversationId(conversationId);
        List<Message> messages = new ArrayList<>();
        chatMemories.forEach(chatMemory ->
                messages.add(this.buildMessageFromChatMemory(chatMemory)));
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 每一次保存最新的一条消息
        aiChatMemoryMapper.insert(this.buildChatMemoryFromMessage(conversationId,
                messages.get(messages.size() - 1)));
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        aiChatMemoryMapper.deleteByConversationId(conversationId);
    }

    public Message buildMessageFromChatMemory(AiChatMemory chatMemory) {
        return switch (chatMemory.getType()) {
            case 1 -> new UserMessage(chatMemory.getContent());
            case 2 -> new SystemMessage(chatMemory.getContent());
            case 3 -> new AssistantMessage(chatMemory.getContent());
            default -> throw new PetException("未知消息类型: " + chatMemory.getType());
        };
    }

    public AiChatMemory buildChatMemoryFromMessage(String conversationId, Message message) {
        AiChatMemory chatMemory = new AiChatMemory();
        chatMemory.setConversationId(conversationId);
        chatMemory.setContent(message.getText());
        switch (message.getMessageType()){
            case USER -> chatMemory.setType(1);
            case SYSTEM -> chatMemory.setType(2);
            case ASSISTANT -> chatMemory.setType(3);
        }
        chatMemory.setTimestamp(LocalDateTime.now());
        return chatMemory;
    }
}
