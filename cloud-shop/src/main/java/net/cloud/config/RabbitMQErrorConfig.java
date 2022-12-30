package net.cloud.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
public class RabbitMQErrorConfig {

    private String orderErrorExchange = "order.error.exchange";

    private String orderErrorQueue = "order.error.queue";

    private String orderErrorRoutingKey = "order.error.routing.key";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 异常交换机
     * @return
     */
    @Bean
    public TopicExchange errorTopicExchange(){
        return new TopicExchange(orderErrorExchange,true,false);
    }

    /**
     * 异常队列
     * @return
     */
    @Bean
    public Queue errorQueue(){
        return new Queue(orderErrorQueue,true);
    }

    /**
     * 队列与交换机进行绑定
     * @return
     */
    @Bean
    public Binding BindingErrorQueueAndExchange(){
        return BindingBuilder.bind(errorQueue()).to(errorTopicExchange()).with(orderErrorRoutingKey);
    }


    /**
     * 配置 RepublishMessageRecoverer
     * 用途：消息重试一定次数后，用特定的routingKey转发到指定的交换机中，方便后续排查和告警
     *
     * RepublishMessageRecoverer 顶层是 MessageRecoverer接口，多个实现类
     * @return
     */
    @Bean
    public MessageRecoverer messageRecoverer(){
        return new RepublishMessageRecoverer(rabbitTemplate,orderErrorExchange,orderErrorRoutingKey);
    }
}