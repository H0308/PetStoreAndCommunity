package org.epsda.pets.utils;

import jakarta.mail.internet.MimeMessage;
import org.epsda.pets.exception.PetException;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 10:12
 *
 * @Author: 憨八嘎
 */
@Component
public class MailUtil {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public MailUtil(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    public void sendMail(String to, String subject, String html) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false);
        mimeMessageHelper.setFrom(mailProperties.getUsername(), mailProperties.getProperties().get("personal"));
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(html, true);

        try {
            javaMailSender.send(mimeMessage);
        } catch (MailAuthenticationException e) {
            throw new PetException("邮件发送失败");
        }
    }
}
