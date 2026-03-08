package org.epsda.pets.controller.user.chat;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.MessageListDTO;
import org.epsda.pets.pojo.vo.ChatMediaVO;
import org.epsda.pets.pojo.vo.CustomerServerInfo;
import org.epsda.pets.pojo.vo.MessageListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.CustomerChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 15:53
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/customer-chat")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class CustomerChatController {

    @Autowired
    private CustomerChatService customerChatService;

    // 获取所有消息（支持分页，一次性最多获取10条数据），包括未读
    // 按照时间进行排序
    @Operation(summary = "获取所有消息")
    @PostMapping("/list")
    public ResultWrapper<PageVO<MessageListVO>> list(@RequestBody MessageListDTO messageListDTO) {
        return ResultWrapper.normal(customerChatService.list(messageListDTO));
    }

    // 获取客服信息
    @Operation(summary = "获取客服信息")
    @GetMapping("/getCustomerServerInfo")
    public ResultWrapper<CustomerServerInfo> getCustomerServerInfo(@NotNull Long userId) {
        return ResultWrapper.normal(customerChatService.getCustomerServerInfo(userId));
    }

    // 读消息
    @Operation(summary = "读消息")
    @PostMapping("/read")
    public ResultWrapper<Boolean> read(@NotNull @RequestParam("userId") Long userId,
                                       @NotNull @RequestParam("sendUserId") Long sendUserId) {
        return ResultWrapper.normal(customerChatService.read(userId, sendUserId));
    }


    // 上传媒体文件
    @Operation(summary = "上传媒体文件")
    @PostMapping("/uploadMedia")
    public ResultWrapper<ChatMediaVO> uploadMedia(@NotNull @RequestParam("mediaType") Integer mediaType,
                                                  @NotNull @RequestPart("file") MultipartFile file) {
        return ResultWrapper.normal(customerChatService.uploadMedia(mediaType, file));
    }

    // 判断是否存在未读消息
    @Operation(summary = "判断是否存在未读消息")
    @PostMapping("/hasUnread")
    public ResultWrapper<Boolean> hasUnread(@NotNull Long userId) {
        return ResultWrapper.normal(customerChatService.hasUnread(userId));
    }
}
