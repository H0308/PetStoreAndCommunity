package org.epsda.pets.common;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 10:26
 *
 * @Author: 憨八嘎
 */
@Data
public class CommonMessage {
    private Long senderId; // 聊天通知设置
    private Long receiverId;
    private Integer type;
    private String title; // 只有系统通知需要设置
    private String content;
    private String mediaUrl; // 聊天通知设置
    private Integer mediaType; // 聊天通知设置
    private Integer onlineStatus; // 聊天通知设置
}
