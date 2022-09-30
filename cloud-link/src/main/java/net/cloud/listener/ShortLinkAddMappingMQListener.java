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
@RabbitListener(queues = "short_link.add.mapping.queue")
public class ShortLinkAddMappingMQListener {

    @Autowired
    private ShortLinkService shortLinkService;

    /**
     *
     * @param eventMessage 消息
     * @param message 原始消息
     * @param channel 信道
     */
    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ShortLinkAddMappingMQListenergit message消息内容:{}",message);

        try {
            //更改消息类型，区分C端和B端
            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_ADD_MAPPING.name());
            shortLinkService.handlerAddShortLink(eventMessage);

        }catch (Exception e){
            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}",eventMessage);
            throw new BizException(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
        log.info("消费成功:{}",eventMessage);
    }
}
