package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.EventMessageType;
import net.cloud.manager.TrafficManager;
import net.cloud.model.EventMessage;
import net.cloud.model.TrafficDO;
import net.cloud.service.TrafficService;
import net.cloud.utils.JsonUtil;
import net.cloud.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Autowired
    private TrafficManager trafficManager;

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void handleTrafficMessage(EventMessage eventMessage) {
        String messageType = eventMessage.getEventMessageType();
        if(EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)){
            //订单已经支付，新增流量包
            String content = eventMessage.getContent();

            Map<String,Object> orderInfoMap = JsonUtil.json2Obj(content, Map.class);

            //还原商品订单信息
            Long accountNo = (Long)orderInfoMap.get("accountNo");
            String outTradeNo = (String) orderInfoMap.get("outTradeNo");
            Integer buyNum = (Integer) orderInfoMap.get("buyNum");
            String productStr = (String) orderInfoMap.get("product");
            ProductVO productVO = JsonUtil.json2Obj(productStr, ProductVO.class);

            log.info("流量包商品信息:{}",productVO);

            //流量包有效期
            LocalDateTime expiredDateTime = LocalDateTime.now().plusDays(productVO.getValidDay());
            //过期时间转成Date
            Date date = Date.from(expiredDateTime.atZone(ZoneId.systemDefault()).toInstant());

            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes() * buyNum)
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo(outTradeNo)
                    .expiredDate(date).build();

            int rows = trafficManager.add(trafficDO);
            log.info("消费消息新增流量包:rows={},{}",rows,trafficDO);
        }
    }
}
