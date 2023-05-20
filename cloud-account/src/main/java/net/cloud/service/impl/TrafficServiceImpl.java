package net.cloud.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.TrafficPageRequest;
import net.cloud.controller.request.UseTrafficRequest;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.EventMessageType;
import net.cloud.exception.BizException;
import net.cloud.feign.ProductFeignService;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.TrafficManager;
import net.cloud.model.EventMessage;
import net.cloud.model.LoginUser;
import net.cloud.model.TrafficDO;
import net.cloud.service.TrafficService;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import net.cloud.utils.TimeUtil;
import net.cloud.vo.ProductVO;
import net.cloud.vo.TrafficVO;
import net.cloud.vo.UseTrafficVO;
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

    @Override
    public boolean deleteExpiredTraffic() {
        return trafficManager.deleteExpiredTraffic();
    }

    /**
     * 扣件流量包:
     *
     * ● ⼤体步骤查
     *   ○ 询⽤户全部可⽤流包
     *   ○ 遍历⽤户可⽤流包
     *     ■ 判断是否更新-⽤⽇期判断（要么都更新过，要么都没更新，根据gmt_modified）
     *       ● 没更新的流包后加⼊【待更新集合】中
     *         ○ 增加【今天剩余可⽤总次数】
     *       ● 已经更新的判断是否超过当天使⽤次数
     *         ○ 如果没超过则增加【今天剩余可⽤总次数】
     *         ○ 超过则忽略
     *   ○ 更新⽤户今⽇流包相关数据
     *   ○ 扣减使⽤的某个流包使⽤次数
     * @param useTrafficRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public JsonData reduce(UseTrafficRequest useTrafficRequest) {
        Long accountNo = useTrafficRequest.getAccountNo();

        //处理流量包，筛选出未更新流量包，当前使用的流量包
        UseTrafficVO useTrafficVO = processTrafficList(accountNo);

        log.info("今天可用总次数:{},当前使用流量包:{}",useTrafficVO.getDayTotalLeftTimes(),useTrafficVO.getCurrentTrafficDO());
        if(useTrafficVO.getCurrentTrafficDO()==null){
            return JsonData.buildResult(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        }

        log.info("待更新流量包列表：{}",useTrafficVO.getUnUpdatedTrafficIds());
        //扣减成功，更新流量包刘表
        if(useTrafficVO.getUnUpdatedTrafficIds().size()>0){
            //更新今日待更新流量包
            trafficManager.batchUpdateUsedTimes(accountNo,useTrafficVO.getUnUpdatedTrafficIds());
        }
        //先更新，再扣件当前使用的流量包
        int rows = trafficManager.addDayUsedTimes(accountNo, useTrafficVO.getCurrentTrafficDO().getId(), 1);
        if(rows!=1){
            throw new BizException(BizCodeEnum.TRAFFIC_EXCEPTION);
        }
        return JsonData.buildSuccess();
    }

    private UseTrafficVO processTrafficList(Long accountNo) {
        //全部流量包
        List<TrafficDO> trafficDOS = trafficManager.selectAvailableTraffics(accountNo);
        if(trafficDOS==null||trafficDOS.size()==0){
            throw new BizException(BizCodeEnum.TRAFFIC_EXCEPTION);
        }

        //天剩余可用总次数
        Integer dayTotalLeftTimes = 0;
        //当前使用
        TrafficDO currentTrafficDO = null;
        //没过期，但是今天没更新的流量包id
        List<Long> unUpdatedTrafficIds = new ArrayList<>();
        //今天日期
        String todayStr = TimeUtil.format(new Date(),"yyyy-MM-dd");
        for (TrafficDO trafficDO : trafficDOS) {
            String trafficUpdateDate = TimeUtil.format(trafficDO.getGmtModified(), "yyyy-MM-dd");
            if(todayStr.equalsIgnoreCase(trafficUpdateDate)){
                //已经更新 单个流量包天剩余总次数=总次数-已经用的
                int dayLeftTimes = trafficDO.getDayLimit() - trafficDO.getDayUsed();
                dayTotalLeftTimes += dayLeftTimes;
                //选取当次使用的流量包
                if(dayLeftTimes>0 && currentTrafficDO == null){
                    currentTrafficDO = trafficDO;
                }
            }else {
                //未更新
                dayTotalLeftTimes = dayTotalLeftTimes + trafficDO.getDayLimit();
                //记录未更新的流量包
                unUpdatedTrafficIds.add(trafficDO.getId());
                //选择当次使用流量包
                if(currentTrafficDO==null){
                    //没过期也没更新肯定可用
                    currentTrafficDO = trafficDO;
                }
            }
        }

        UseTrafficVO useTrafficVO = new UseTrafficVO(dayTotalLeftTimes, currentTrafficDO, unUpdatedTrafficIds);
        return useTrafficVO;
    }

    private TrafficVO beanProcess(TrafficDO trafficDO) {
        TrafficVO trafficVO = new TrafficVO();
        BeanUtils.copyProperties(trafficDO,trafficVO);
        return trafficVO;
    }
}
