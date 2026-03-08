package org.epsda.pets.service.impl;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.pojo.dto.NotifyEmailDTO;
import org.epsda.pets.pojo.message.NotifyEmailInfo;
import org.epsda.pets.service.AdminNotifyService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 10:41
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminNotifyServiceImpl implements AdminNotifyService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Boolean send(NotifyEmailDTO notifyEmailDTO) {
        if (notifyEmailDTO == null) {
            throw new PetException("邮件信息错误");
        }

        Long userId = notifyEmailDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        String email = notifyEmailDTO.getEmail();
        String subject = notifyEmailDTO.getSubject();
        String content = notifyEmailDTO.getContent();
        NotifyEmailInfo notifyEmailInfo =
                new NotifyEmailInfo(email, subject, content);

        rabbitTemplate.convertAndSend("", Constants.NOTIFY_EMAIL_QUEUE, notifyEmailInfo);
        return true;
    }
}
