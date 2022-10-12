package net.cloud.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.EventMessageType;
import net.cloud.exception.BizException;
import net.cloud.model.EventMessage;
import net.cloud.service.ShortLinkService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
//rabbit mq 懒加载模式，需要配置消费者监听才会创建
@RabbitListener(queues = "short_link.update.link.queue")
//第二种方法，没有queue就自动创建,但是可能不会绑定到对应的交换机，可以避免出错
//@RabbitListener(queuesToDeclare = {@Queue("short_link.add.link.queue")})
public class ShortLinkUpdateLinkMQListener {

    @Autowired
    private ShortLinkService shortLinkService;

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
        log.info("监听到消息ShortLinkUpdateLinkMQListener message消息内容:{}",message);

        try {
            //业务代码
            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_UPDATE_LINK.name());
            //TODO
        }catch (Exception e){
            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}",eventMessage);
            throw new BizException(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
        log.info("消费成功:{}",eventMessage);
    }
}
