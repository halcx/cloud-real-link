package net.cloud.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import net.cloud.model.EventMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
//rabbit mq 懒加载模式，需要配置消费者监听才会创建
@RabbitListener(queues = "short_link.error.queue")
//第二种方法，没有queue就自动创建,但是可能不会绑定到对应的交换机，可以避免出错
//@RabbitListener(queuesToDeclare = {@Queue("short_link.add.link.queue")})
public class ShortLinkErrorMQListener {

    /**
     * 消费者
     * 消费者消费异常情况：
     *      1、业务代码进行重试
     *      2、组件重试
     * @param eventMessage 消息
     * @param message 原始消息
     * @param channel 信道
     */
    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.error("告警：监听到消息ShortLinkErrorMQListener eventMessage:{}",eventMessage);
        log.error("告警：监听到消息ShortLinkErrorMQListener message:{}",message);
        log.error("告警成功，发送通知短信");
    }
}
