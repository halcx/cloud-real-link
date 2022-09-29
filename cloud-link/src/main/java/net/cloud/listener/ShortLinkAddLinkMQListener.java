package net.cloud.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import net.cloud.model.EventMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
//rabbit mq 懒加载模式，需要配置消费者监听才会创建
@RabbitListener(queues = "short_link.add.link.queue")
//第二种方法，没有queue就自动创建,但是可能不会绑定到对应的交换机，可以避免出错
//@RabbitListener(queuesToDeclare = {@Queue("short_link.add.link.queue")})
public class ShortLinkAddLinkMQListener {

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
        log.info("监听到消息ShortLinkAddLinkMQListener message消息内容:{}",message);
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            //TODO 处理业务逻辑，消费消息
            int i = 1/0;
        }catch (Exception e){
            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}",eventMessage);
            throw new BizException(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
        log.info("消费成功:{}",eventMessage);
        //确认消息消费成功
//        channel.basicAck(tag,false);
    }
}
