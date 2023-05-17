package net.cloud.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.TrafficPageRequest;
import net.cloud.enums.EventMessageType;
import net.cloud.feign.ProductFeignService;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.TrafficManager;
import net.cloud.model.EventMessage;
import net.cloud.model.LoginUser;
import net.cloud.model.TrafficDO;
import net.cloud.service.TrafficService;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import net.cloud.vo.ProductVO;
import net.cloud.vo.TrafficVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Autowired
    private TrafficManager trafficManager;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void handleTrafficMessage(EventMessage eventMessage) {

        Long accountNo = eventMessage.getAccountNo();

        String messageType = eventMessage.getEventMessageType();
        if(EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)){
            //订单已经支付，新增流量包
            String content = eventMessage.getContent();

            Map<String,Object> orderInfoMap = JsonUtil.json2Obj(content, Map.class);

            //还原商品订单信息
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
        }else if(EventMessageType.TRAFFIC_FREE_INIT.name().equalsIgnoreCase(messageType)){
            //发放免费流量包
            //这里需要RPC调用，因为我们不知道免费流量包每年创建多少次短链

            //商品Id
            Long productId = Long.valueOf(eventMessage.getBizId());

            JsonData jsonDatao = productFeignService.detail(productId);

            //get转成vo
            ProductVO productVO = jsonDatao.getData(new TypeReference<ProductVO>() {});

            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes())
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo("free_init")
                    .expiredDate(new Date()).build();

            trafficManager.add(trafficDO);
        }
    }

    @Override
    public Map<String, Object> pageAvailable(TrafficPageRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        IPage<TrafficDO> trafficDOIPage = trafficManager.pageAvailable(page, size, loginUser.getAccountNo());
        //获取流量包列表
        List<TrafficDO> records = trafficDOIPage.getRecords();

        List<TrafficVO> trafficVOList = records.stream().map(obj -> beanProcess(obj)).collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>(3);
        map.put("total_record",trafficDOIPage.getTotal());
        map.put("total_page",trafficDOIPage.getPages());
        map.put("current_data",trafficVOList);

        return map;
    }

    @Override
    public TrafficVO detail(Long trafficId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        TrafficDO trafficDO = trafficManager.findByIdAndAccountNo(trafficId, loginUser.getAccountNo());
        return beanProcess(trafficDO);
    }

    private TrafficVO beanProcess(TrafficDO trafficDO) {
        TrafficVO trafficVO = new TrafficVO();
        BeanUtils.copyProperties(trafficDO,trafficVO);
        return trafficVO;
    }
}
