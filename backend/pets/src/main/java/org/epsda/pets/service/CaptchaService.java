package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/22
 * Time: 22:48
 *
 * @Author: 憨八嘎
 */
public interface CaptchaService {
    Boolean getEmailCaptcha(String email);

    Boolean checkEmailCaptcha(String email, String captcha);
}
