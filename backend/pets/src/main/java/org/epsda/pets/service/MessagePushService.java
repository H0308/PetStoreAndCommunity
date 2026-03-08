package org.epsda.pets.service;

import org.epsda.pets.common.CommonMessage;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 18:49
 *
 * @Author: 憨八嘎
 */
public interface MessagePushService {
    void saveAndSendSystemMessage(CommonMessage commonMessage, Integer systemMessageType);
    void saveAndSendChatMessage(CommonMessage commonMessage);
    void sendMessage(CommonMessage commonMessage);
}
