package net.cloud.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import net.cloud.model.EventMessage;
import net.cloud.service.ProductOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queuesToDeclare = {@Queue("order.close.queue")})
public class ProductOrderMQListener {

    @Autowired
    private ProductOrderService productOrderService;


    @RabbitHandler
    public void productOrderHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ProductOrderMQListener message消息内容:{}", message);
        try {
            //关闭订单
            productOrderService.closeProductOrder(eventMessage);

        } catch (Exception e) {
            //处理业务异常，还有进⾏其他操作，⽐如记录失败原因
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);
    }
}