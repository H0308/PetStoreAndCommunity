package org.epsda.pets.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 18:16
 *
 * @Author: 憨八嘎
 */
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("基于 SpringBoot 的宠物商城与社区系统")
                        .version("1.0")
                        .description("这是一个基于 SpringBoot3 + Vue3 的宠物商城与社区系统API文档"));
    }
}
