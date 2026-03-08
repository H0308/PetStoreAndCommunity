package org.epsda.pets.listener;

import lombok.SneakyThrows;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.pojo.message.NotifyEmailInfo;
import org.epsda.pets.utils.MailUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 13:20
 *
 * @Author: 憨八嘎
 */
@Component
@RabbitListener(queues = Constants.NOTIFY_EMAIL_QUEUE)
public class NotifyQueueListener {
    @Autowired
    private MailUtil mailUtil;

    @SneakyThrows
    @RabbitHandler
    public void notifyEmailHandler(NotifyEmailInfo notifyEmailInfo) {
        String email = notifyEmailInfo.getEmail();
        String subject = notifyEmailInfo.getSubject();
        String content = notifyEmailInfo.getContent();
        String logoUrl = "https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/%E5%AE%A0%E7%89%A9.ico";
        String html = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>%s - 小橘岛</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f5f5f5;font-family:'PingFang SC','Microsoft YaHei',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#FF8A5B,#ffb08a);padding:36px 40px;text-align:center;">
                              <img src="%s" alt="小橘岛" width="52" height="52"
                                style="border-radius:50%%;background:#fff;padding:6px;display:block;margin:0 auto 12px;" />
                              <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:600;letter-spacing:1px;">小橘岛</h1>
                              <p style="margin:6px 0 0;color:rgba(255,255,255,0.85);font-size:13px;">宠物商城与社区系统</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:40px 40px 32px;">
                              <h2 style="margin:0 0 24px;color:#29343D;font-size:20px;font-weight:600;">%s</h2>
                              <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                                <tr>
                                  <td style="background-color:#fff8f5;border-radius:8px;padding:24px;">
                                    <p style="margin:0;color:#444;font-size:15px;line-height:1.9;">%s</p>
                                  </td>
                                </tr>
                              </table>
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="background-color:#f9f9f9;border-left:3px solid #FF8A5B;border-radius:4px;padding:14px 16px;">
                                    <p style="margin:0;color:#aaa;font-size:13px;line-height:1.7;">如有疑问，请联系平台客服处理。</p>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#fafafa;border-top:1px solid #f0f0f0;padding:24px 40px;text-align:center;">
                              <p style="margin:0 0 6px;color:#aaa;font-size:12px;">此邮件由系统自动发送，请勿直接回复</p>
                              <p style="margin:0;color:#ccc;font-size:12px;">© 2026 小橘岛 · 宠物商城与社区系统</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(subject, logoUrl, subject, content);
        mailUtil.sendMail(email, subject, html);
    }
}
