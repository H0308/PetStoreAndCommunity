package org.epsda.pets.controller.admin.chat;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.HistoryMessageDTO;
import org.epsda.pets.pojo.dto.LatestMessageDTO;
import org.epsda.pets.pojo.dto.MessageListDTO;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.AdminChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 17:08
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/chat")
public class AdminChatController {

    @Autowired
    private AdminChatService adminChatService;

    // 获取最近的消息列表
    @Operation(summary = "获取最近的消息列表")
    @PostMapping("/latest")
    public ResultWrapper<List<LatestMessageListVO>> latest(@RequestBody LatestMessageDTO latestMessageDTO) {
        return ResultWrapper.normal(adminChatService.latest(latestMessageDTO));
    }

    // 获取历史消息
    @Operation(summary = "获取历史消息")
    @PostMapping("/history")
    public ResultWrapper<PageVO<MessageListVO>> history(@RequestBody HistoryMessageDTO historyMessageDTO) {
        return ResultWrapper.normal(adminChatService.history(historyMessageDTO));
    }

    // 读消息
    @Operation(summary = "读消息")
    @PostMapping("/read")
    public ResultWrapper<Boolean> read(@NotNull @RequestParam("userId") Long userId,
                                       @NotNull @RequestParam("sendUserId") Long sendUserId) {
        return ResultWrapper.normal(adminChatService.read(userId, sendUserId));
    }

    // 获取用户信息
    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    public ResultWrapper<ChatUserInfo> getUserInfo(@NotNull @RequestParam("userId") Long userId,
                                                   @NotNull @RequestParam("sendUserId") Long sendUserId) {
        return ResultWrapper.normal(adminChatService.getUserInfo(userId, sendUserId));
    }
}
