package org.epsda.pets.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/23
 * Time: 19:34
 *
 * @Author: 憨八嘎
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfig {
    private Integer width;
    private Integer height;
    private Session session; // 存储Session数据

    @Data
    public static class Session{
        private String key_name;
        private String date_name;
    }
}
