package org.epsda.pets.config;

import org.epsda.pets.constants.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 9:22
 *
 * @Author: 憨八嘎
 */
@Configuration
public class RabbitConfig {
    // 返回用于序列化和反序列化的Json对象
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    // 自定义RabbitTemplate对象，使其支持Json序列化和反序列化
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter()); // 设置消息转换器
        return template;
    }

    // 基础模式队列——注册邮件发送
    @Bean(Constants.REGISTER_EMAIL_QUEUE)
    public Queue registerQueue() {
        return QueueBuilder.durable(Constants.REGISTER_EMAIL_QUEUE).build();
    }
    // 基础模式队列——通知邮件发送
    @Bean(Constants.NOTIFY_EMAIL_QUEUE)
    public Queue notifyQueue() {
        return QueueBuilder.durable(Constants.NOTIFY_EMAIL_QUEUE).build();
    }
    // 路由模式队列——帖子审核
    @Bean(Constants.POST_AUDIT_QUEUE)
    public Queue postAuditQueue() {
        return QueueBuilder.durable(Constants.POST_AUDIT_QUEUE).build();
    }
    // 路由模式队列——评论审核
    @Bean(Constants.COMMENT_AUDIT_QUEUE)
    public Queue commentAuditQueue() {
        return QueueBuilder.durable(Constants.COMMENT_AUDIT_QUEUE).build();
    }
    // 路由模式交换机——审核系统
    @Bean(Constants.AUDIT_EXCHANGE)
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(Constants.AUDIT_EXCHANGE).durable(true).build();
    }
    // 绑定帖子审核队列和交换机
    @Bean(Constants.POST_AUDIT_BINDING_TAG)
    public Binding postAuditBinding(@Qualifier(Constants.AUDIT_EXCHANGE) DirectExchange directExchange,
                                    @Qualifier(Constants.POST_AUDIT_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with(Constants.POST_AUDIT_ROUTING_KEY);
    }
    // 绑定评论审核队列和交换机
    @Bean(Constants.COMMENT_AUDIT_BINDING_TAG)
    public Binding commentAuditBinding(@Qualifier(Constants.AUDIT_EXCHANGE) DirectExchange directExchange,
                                    @Qualifier(Constants.COMMENT_AUDIT_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with(Constants.COMMENT_AUDIT_ROUTING_KEY);
    }
}
