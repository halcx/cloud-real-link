package net.cloud.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import net.cloud.model.EventMessage;
import net.cloud.service.TrafficService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queuesToDeclare = {
        @Queue("order.traffic.queue")
})
@Slf4j
public class TrafficMQListener {
    @Autowired
    private TrafficService trafficService;

    public void trafficHandler(EventMessage eventMessage, Message message, Channel channel){
        log.info("监听到消息trafficHandler:{}",eventMessage);
        try {
            trafficService.handleTrafficMessage(eventMessage);

        } catch (Exception e) {
            //处理业务异常，还有进⾏其他操作，⽐如记录失败原因
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);
    }
}
