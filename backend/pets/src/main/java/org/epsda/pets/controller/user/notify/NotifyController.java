package org.epsda.pets.controller.user.notify;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.NotifyListDTO;
import org.epsda.pets.pojo.vo.NotifyListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.plaf.PanelUI;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 20:17
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/notify")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class NotifyController {
    @Autowired
    private NotifyService notifyService;

    // 获取通知列表（支持按照消息类型筛选和分页）
    @Operation(summary = "获取通知列表")
    @PostMapping("/list")
    public ResultWrapper<PageVO<NotifyListVO>> list(@Validated @RequestBody NotifyListDTO notifyListDTO) {
        return ResultWrapper.normal(notifyService.list(notifyListDTO));
    }

    // 读消息
    @Operation(summary = "读消息")
    @PostMapping("/read")
    public ResultWrapper<Boolean> read(@NotNull @RequestParam("userId") Long userId,
                                       @NotNull @RequestParam("notifyId") Long notifyId) {
        return ResultWrapper.normal(notifyService.read(userId, notifyId));
    }

    // 标记所有消息已读
    @Operation(summary = "标记所有消息已读")
    @PostMapping("/readAll")
    public ResultWrapper<Boolean> readAll(@NotNull @RequestParam("userId") Long userId,
                                       @NotNull @RequestParam("notifyIds") List<Long> notifyIds) {
        return ResultWrapper.normal(notifyService.readAll(userId, notifyIds));
    }

    // 获取未读消息个数
    @Operation(summary = "获取未读消息个数")
    @PostMapping("/unreadCount")
    public ResultWrapper<Long> unreadCount(@NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(notifyService.unreadCount(userId));
    }

    // 删除消息
    @Operation(summary = "删除消息")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("userId") Long userId,
                                      @NotNull @RequestParam("notifyId") Long notifyId) {
        return ResultWrapper.normal(notifyService.delete(userId, notifyId));
    }
}
