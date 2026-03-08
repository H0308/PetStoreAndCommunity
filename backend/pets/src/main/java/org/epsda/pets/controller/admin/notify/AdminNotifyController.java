package org.epsda.pets.controller.admin.notify;

import io.swagger.v3.oas.annotations.Operation;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.NotifyEmailDTO;
import org.epsda.pets.service.AdminNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 10:36
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/notify")
public class AdminNotifyController {

    @Autowired
    private AdminNotifyService adminNotifyService;

    // 编辑邮件发送给用户
    @Operation(summary = "编辑邮件发送给用户")
    @PostMapping("/send")
    public ResultWrapper<Boolean> send(@Validated @RequestBody NotifyEmailDTO notifyEmailDTO) {
        return ResultWrapper.normal(adminNotifyService.send(notifyEmailDTO));
    }
}
