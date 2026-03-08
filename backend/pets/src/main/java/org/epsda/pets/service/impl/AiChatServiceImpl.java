package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.AiChatMemoryMapper;
import org.epsda.pets.mapper.AiChatMetadataMapper;
import org.epsda.pets.pojo.AiChatMemory;
import org.epsda.pets.pojo.AiChatMetadata;
import org.epsda.pets.pojo.dto.ChatHistoryDTO;
import org.epsda.pets.pojo.dto.ChatListDTO;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.AiChatService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/03/03
 * Time: 21:03
 *
 * @Author: 憨八嘎
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AiChatMetadataMapper aiChatMetadataMapper;
    @Autowired
    private AiChatMemoryMapper aiChatMemoryMapper;
    @Autowired
    private ChatMemoryRepository chatMemoryRepository;

    @Override
    public PageVO<ChatListVO> list(ChatListDTO chatListDTO) {
        if (chatListDTO == null) {
            throw new PetException("用户信息错误，获取对话列表失败");
        }

        Long pageSize = chatListDTO.getPageSize();
        Long currentPage = chatListDTO.getCurrentPage();
        Long userId = chatListDTO.getUserId();
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);

        Page<AiChatMetadata> aiChatMetadataPage = new Page<>(currentPage, pageSize);
        Page<AiChatMetadata> aiChatMetadataPages = aiChatMetadataMapper.selectPage(aiChatMetadataPage,
                new LambdaQueryWrapper<AiChatMetadata>()
                        .eq(AiChatMetadata::getUserId, userId)
                        .eq(AiChatMetadata::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                        .orderByDesc(AiChatMetadata::getCreateTime));
        List<AiChatMetadata> records = aiChatMetadataPages.getRecords();
        List<ChatListVO> chatListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            records.forEach(chatMetadata ->
                    chatListVOS.add(this.buildChatListVOFromChatMetaData(chatMetadata)));
        }

        return PageVO.<ChatListVO>builder()
                .currentPage(aiChatMetadataPages.getCurrent())
                .totalPages(aiChatMetadataPages.getPages())
                .totalCount(aiChatMetadataPages.getTotal())
                .totalRecords(chatListVOS)
                .build();
    }

    @Override
    public ChatCreateVO create(Long userId) {
        // 使用UUID创建会话
        String conversationId = UUID.randomUUID().toString();
        // 创建会话元数据
        AiChatMetadata aiChatMetadata = new AiChatMetadata();
        // 先不设置标题，待用户发送第一条消息后再设置
        aiChatMetadata.setConversationId(conversationId);
        aiChatMetadata.setUserId(userId);
        boolean insertRet = aiChatMetadataMapper.insert(aiChatMetadata) == 1;
        if (!insertRet) {
            throw new PetException("新建会话失败");
        }

        return ChatCreateVO.builder()
                .chatId(aiChatMetadata.getId())
                .conversationId(conversationId)
                .build();
    }

    @Override
    public String title(Long chatId, String firstMessage) {
        // 从用户的第一条消息进行获取
        String title = firstMessage.length() > 20 ? firstMessage.substring(0, 20) : firstMessage;

        AiChatMetadata metadata = aiChatMetadataMapper.selectOne(new LambdaQueryWrapper<AiChatMetadata>()
                .eq(AiChatMetadata::getId, chatId)
                .eq(AiChatMetadata::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (metadata == null) {
            throw new PetException("获取对话信息失败");
        }

        boolean updateRet = aiChatMetadataMapper.update(new LambdaUpdateWrapper<AiChatMetadata>()
                .eq(AiChatMetadata::getId, chatId)
                .set(AiChatMetadata::getTitle, title)) == 1;

        if (!updateRet) {
            throw new PetException("对话更新失败");
        }

        return title;
    }

    @Override
    public PageVO<ChatHistoryVO> history(ChatHistoryDTO chatHistoryDTO) {
        if (chatHistoryDTO == null) {
            throw new PetException("用户信息错误，获取对话列表失败");
        }

        Long pageSize = chatHistoryDTO.getPageSize();
        Long currentPage = chatHistoryDTO.getCurrentPage();
        Long userId = chatHistoryDTO.getUserId();
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long chatId = chatHistoryDTO.getChatId();
        AiChatMetadata aiChatMetadata = aiChatMetadataMapper.selectOne(new LambdaQueryWrapper<AiChatMetadata>().eq(AiChatMetadata::getId, chatId).eq(AiChatMetadata::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (aiChatMetadata == null) {
            throw new PetException("对话信息错误");
        }

        Page<AiChatMemory> aiChatMemoryPage = new Page<>(currentPage, pageSize);
        Page<AiChatMemory> aiChatMemoryPages = aiChatMemoryMapper.selectPage(aiChatMemoryPage,
                new LambdaQueryWrapper<AiChatMemory>()
                        .eq(AiChatMemory::getConversationId, aiChatMetadata.getConversationId())
                        .orderByDesc(AiChatMemory::getTimestamp));
        List<AiChatMemory> records = aiChatMemoryPages.getRecords();
        List<ChatHistoryVO> chatHistoryVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            records.forEach(chatMemory ->
                    chatHistoryVOS.add(this.buildChatHistoryVOFromChatMemory(chatMemory)));
        }

        return PageVO.<ChatHistoryVO>builder()
                .currentPage(aiChatMemoryPages.getCurrent())
                .totalPages(aiChatMemoryPages.getPages())
                .totalCount(aiChatMemoryPages.getTotal())
                .totalRecords(chatHistoryVOS)
                .build();
    }

    @Override
    public Boolean delete(Long chatId, Long userId) {
        if (chatId == null || userId == null) {
            throw new PetException("用户信息错误，删除对话失败");
        }

        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 删除元数据表
        AiChatMetadata aiChatMetadata = aiChatMetadataMapper.selectOne(new LambdaQueryWrapper<AiChatMetadata>()
                .eq(AiChatMetadata::getId, chatId)
                .eq(AiChatMetadata::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (aiChatMetadata == null) {
            throw new PetException("对话信息不存在，删除失败");
        }

        boolean updateRet = aiChatMetadataMapper.update(new LambdaUpdateWrapper<AiChatMetadata>()
                .eq(AiChatMetadata::getId, chatId)
                .set(AiChatMetadata::getDeleteFlag, Constants.DELETED_FLAG)) == 1;
        if (!updateRet) {
            throw new PetException("对话信息删除失败");
        }

        chatMemoryRepository.deleteByConversationId(aiChatMetadata.getConversationId());

        return true;
    }

    private ChatHistoryVO buildChatHistoryVOFromChatMemory(AiChatMemory aiChatMemory) {
        if (aiChatMemory == null) {
            return null;
        }

        return ChatHistoryVO.builder()
                .chatId(aiChatMemory.getId())
                .content(aiChatMemory.getContent())
                .type(aiChatMemory.getType())
                .createTime(aiChatMemory.getTimestamp())
                .build();
    }

    private ChatListVO buildChatListVOFromChatMetaData(AiChatMetadata aiChatMetadata) {
        if (aiChatMetadata == null) {
            return null;
        }

        return ChatListVO.builder()
                .title(aiChatMetadata.getTitle())
                .chatId(aiChatMetadata.getId())
                .build();
    }
}
