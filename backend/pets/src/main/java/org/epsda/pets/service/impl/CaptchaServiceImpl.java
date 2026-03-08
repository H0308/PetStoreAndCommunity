package org.epsda.pets.service.impl;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.pojo.message.NotifyEmailInfo;
import org.epsda.pets.service.CaptchaService;
import org.epsda.pets.utils.CaptchaUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/22
 * Time: 22:49
 *
 * @Author: 憨八嘎
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Boolean getEmailCaptcha(String email) {
        if (!StringUtils.hasText(email)) {
            throw new PetException("邮箱为空，发送验证码失败");
        }
        String rejectKey = Constants.REDIS_EMAIL_CAPTCHA_PREFIX + email +
                Constants.CAPTCHA_LIMIT_SUFFIX;
        String rejectCode = stringRedisTemplate.opsForValue().get(rejectKey);
        if (rejectCode != null) {
            throw new PetException("请" + Constants.CAPTCHA_LIMIT_TIME_MINUTES + "分钟后重试");
        }

        // 生成6位数字验证码
        String captcha = CaptchaUtil.generateAlphanumericCaptcha(Constants.CAPTCHA_LENGTH);

        String key = Constants.REDIS_EMAIL_CAPTCHA_PREFIX + email;
        stringRedisTemplate.opsForValue().set(key, captcha,
                Constants.CAPTCHA_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(rejectKey, Constants.CAPTCHA_LIMIT_SUFFIX,
                Constants.CAPTCHA_LIMIT_TIME_MINUTES, TimeUnit.MINUTES);
        NotifyEmailInfo notifyEmailInfo =
                new NotifyEmailInfo(email, "邮箱验证码", "您的邮箱验证码是：" + captcha);
        rabbitTemplate.convertAndSend("", Constants.NOTIFY_EMAIL_QUEUE, notifyEmailInfo);

        return true;
    }

    @Override
    public Boolean checkEmailCaptcha(String email, String captcha) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(captcha)) {
            throw new PetException("邮箱或者验证码为空，校验验证码失败");
        }

        String rejectKey = Constants.REDIS_EMAIL_CAPTCHA_PREFIX + email +
                Constants.CAPTCHA_LIMIT_SUFFIX;
        String key = Constants.REDIS_EMAIL_CAPTCHA_PREFIX + email;
        String correctCaptcha = stringRedisTemplate.opsForValue().get(key);
        if (correctCaptcha == null) {
            throw new PetException("验证码已经过期");
        }

        if (correctCaptcha.equals(captcha)) {
            // 删除验证码
            stringRedisTemplate.opsForValue().getAndDelete(key);
            stringRedisTemplate.opsForValue().getAndDelete(rejectKey);
            return true;
        }

        return false;
    }
}
