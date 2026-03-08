package org.epsda.pets.controller.user.chat;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.ChatHistoryDTO;
import org.epsda.pets.pojo.dto.ChatListDTO;
import org.epsda.pets.pojo.vo.ChatCreateVO;
import org.epsda.pets.pojo.vo.ChatHistoryVO;
import org.epsda.pets.pojo.vo.ChatListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.AiChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 10:46
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/ai")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class AiController {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private AiChatService aiChatService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chatClientStream(@NotNull String message, @NotNull String conversationId) {
        return chatClient.prompt()
                .advisors(spec ->
                        spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message).stream().content();
    }

    // 获取所有会话
    @RequestMapping("/list")
    public ResultWrapper<PageVO<ChatListVO>> list(@Validated @RequestBody ChatListDTO chatListDTO) {
        return ResultWrapper.normal(aiChatService.list(chatListDTO));
    }

    // 创建新会话
    @RequestMapping("/create")
    public ResultWrapper<ChatCreateVO> create(@NotNull Long userId) {
        // 创建会话编号并返回，前端先默认显示新建会话
        return ResultWrapper.normal(aiChatService.create(userId));
    }

    // 获取会话标题
    @RequestMapping("/title")
    public ResultWrapper<String> title(@NotNull Long chatId, @NotNull String firstMessage) {
        return ResultWrapper.normal(aiChatService.title(chatId, firstMessage));
    }

    // 获取历史对话记录
    @RequestMapping("/history")
    public ResultWrapper<PageVO<ChatHistoryVO>> history(@Validated @RequestBody ChatHistoryDTO chatHistoryDTO) {
        return ResultWrapper.normal(aiChatService.history(chatHistoryDTO));
    }

    // 删除会话
    @RequestMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("chatId") Long chatId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(aiChatService.delete(chatId, userId));
    }
}
