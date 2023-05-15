package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.EventMessageType;
import net.cloud.model.EventMessage;
import net.cloud.service.TrafficService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void handleTrafficMessage(EventMessage eventMessage) {
        String messageType = eventMessage.getEventMessageType();
        if(EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)){
            //订单已经支付，新增流量包
            String content = eventMessage.getContent();
        }
    }
}
