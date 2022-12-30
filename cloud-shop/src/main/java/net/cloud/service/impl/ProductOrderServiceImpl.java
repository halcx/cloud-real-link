package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.config.RabbitMQConfig;
import net.cloud.constant.TimeConstant;
import net.cloud.controller.request.ConfirmOrderRequest;
import net.cloud.controller.request.ProductOrderPageRequest;
import net.cloud.enums.*;
import net.cloud.exception.BizException;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.ProductManager;
import net.cloud.manager.ProductOrderManager;
import net.cloud.model.EventMessage;
import net.cloud.model.LoginUser;
import net.cloud.model.ProductDO;
import net.cloud.model.ProductOrderDO;
import net.cloud.service.ProductOrderService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import net.cloud.vo.PayInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {

    @Autowired
    private ProductOrderManager productOrderManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Override
    public Map<String, Object> page(ProductOrderPageRequest productOrderPageRequest) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map<String, Object> pageResult = productOrderManager.page(
                productOrderPageRequest.getPage(), productOrderPageRequest.getSize()
                , accountNo,
                productOrderPageRequest.getState());
        return pageResult;
    }

    @Override
    public String queryProductOrderState(String outTradeNo) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        ProductOrderDO productOrderDO = productOrderManager.findByOutTradeNoAndAccountNo(outTradeNo, accountNo);
        if(productOrderDO==null){
            return "";
        }else {
            String state = productOrderDO.getState();
            return state;
        }
    }

    /**
     * 业务流程：
     * - 防重复提交
     * - 获取最新的流量包价格
     * - 订单验价
     *      - 如果有优惠券或者其他抵扣
     *      - 验证前端显示和后台计算价格
     * - 创建订单对象，保存数据库
     * - 发送延迟消息，用于自动关单
     * - 创建支付信息，对接三方支付
     * - 回调更新订单状态
     * - 支付成功创建流量包
     * @param request
     * @return
     */
    @Override
    public JsonData confirmOrder(ConfirmOrderRequest request) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        //随机获取32位订单号
        String orderOutTradeNum = CommonUtil.getStringNumRandom(32);

        ProductDO productDO = productManager.findDetailById(request.getProductId());

        //验证价格
        this.checkPrice(productDO,request);

        //创建订单
        ProductOrderDO productOrderDO = this.saveProductOrder(request,loginUser,orderOutTradeNum,productDO);

        //创建支付对象
        PayInfoVO payInfoVO = PayInfoVO.builder().accountNo(loginUser.getAccountNo())
                .outTradeNo(orderOutTradeNum)
                .clientType(request.getClientType())
                .payType(request.getPayType())
                .title(productDO.getTitle())
                .description(productDO.getDetail())
                .payFee(request.getPayAmount())
                .orderPayTimeoutMills(TimeConstant.ORDER_PAY_TIMEOUT_MILLS).build();

        //发送延迟消息 用于关单
        EventMessage eventMessage = EventMessage.builder().eventMessageType(EventMessageType.PRODUCT_ORDER_NEW.name())
                .accountNo(loginUser.getAccountNo())
                .bizId(orderOutTradeNum).build();

        rabbitTemplate.convertAndSend(rabbitMQConfig.getOrderEventExchange(),rabbitMQConfig.getOrderCloseDelayRoutingKey(),eventMessage);

        //TODO 调用支付信息

        return JsonData.buildSuccess();
    }

    /**
     * 延迟消息的时间，需要比订单过期时间长一点，这样就不存在查询的时候，用户还能支付成功
     *
     * 查询订单是否存在，如果已经支付，则正常结束
     * 如果订单未支付，主动调用第三方支付平台查询订单状态
     * - 确认未支付，本地取消订单
     * - 如果第三方已经支付，主动的把订单状态改成已经支付，这种情况的原因可能是支付通道回调有问题。然后我们要触发支付后的动作。
     * - 如何触发呢？RPC还是？
     * @param eventMessage
     * @return
     */
    @Override
    public boolean closeProductOrder(EventMessage eventMessage) {
        String outTradeNo = eventMessage.getBizId();
        Long accountNo = eventMessage.getAccountNo();
        //判断订单是否存在
        ProductOrderDO productOrderDO = productOrderManager.findByOutTradeNoAndAccountNo(outTradeNo, accountNo);

        if(productOrderDO==null){
            //订单不存在
            log.warn("订单不存在");
            return true;
        }

        if(productOrderDO.getState().equalsIgnoreCase(ProductOrderStateEnum.PAY.name())){
            //已经支付
            log.info("直接确认消息，订单已经支付:{}",eventMessage);
            return true;
        }

        //未支付
        if(productOrderDO.getState().equalsIgnoreCase(ProductOrderStateEnum.NEW.name())){
            //向第三方查询状态
            PayInfoVO payInfoVO = new PayInfoVO();
            payInfoVO.setPayType(productOrderDO.getPayType());
            payInfoVO.setOutTradeNo(outTradeNo);
            payInfoVO.setAccountNo(accountNo);
            //TODO 去第三方平台查询

            String payResult = "";

            if(StringUtils.isBlank(payResult)){
                //如果为空，则未支付成功，取消本地的订单
                productOrderManager.updateOrderPayState(outTradeNo,accountNo,
                        ProductOrderStateEnum.CANCEL.name(),ProductOrderStateEnum.NEW.name());
                log.info("未支付成功，本地取消订单:{}",eventMessage);
            }else {
                //支付成功，主动把订单状态更新成支付
                log.warn("支付成功，但是微信回调通知失败，需要排查问题:{}",eventMessage);
                productOrderManager.updateOrderPayState(outTradeNo,accountNo,
                        ProductOrderStateEnum.PAY.name(),ProductOrderStateEnum.NEW.name());
                //TODO 触发支付成功后的逻辑
            }
        }

        return true;
    }

    private ProductOrderDO saveProductOrder(ConfirmOrderRequest request, LoginUser loginUser, String orderOutTradeNum, ProductDO productDO) {
        ProductOrderDO productOrderDO = new ProductOrderDO();
        //设置用户信息
        productOrderDO.setAccountNo(loginUser.getAccountNo());
        productOrderDO.setNickname(loginUser.getUsername());

        //设置商品信息
        productOrderDO.setProductId(productDO.getId());
        productOrderDO.setProductTitle(productDO.getTitle());
        productOrderDO.setProductSnapshot(JsonUtil.obj2Json(productDO));
        productOrderDO.setProductAmount(productDO.getAmount());

        //设置订单信息
        productOrderDO.setBuyNum(request.getBuyNum());
        productOrderDO.setOutTradeNo(orderOutTradeNum);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);

        //价格信息
        //实际支付总价
        productOrderDO.setPayAmount(request.getPayAmount());
        //总价 没有使用优惠券
        productOrderDO.setTotalAmount(request.getTotalAmount());
        //订单状态
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        //支付类型
        productOrderDO.setPayType(ProductOrderPayEnum.valueOf(request.getPayType()).name());

        //发票信息
        productOrderDO.setBillType(BillTypeEnum.valueOf(request.getBillType()).name());
        productOrderDO.setBillHeader(request.getBillHeader());
        productOrderDO.setBillReceiverEmail(request.getBillReceiverEmail());
        productOrderDO.setBillReceiverPhone(request.getBillReceiverPhone());
        productOrderDO.setBillContent(request.getBillContent());

        //插入数据库
        productOrderManager.add(productOrderDO);

        return productOrderDO;
    }

    private void checkPrice(ProductDO productDO, ConfirmOrderRequest request) {
        //后台计算价格
        BigDecimal bizTotal = BigDecimal.valueOf(request.getBuyNum()).multiply(productDO.getAmount());

        //前端传递总价和后端计算总价格是否一致，如果有优惠券了，也要在这边进行计算和抵扣
        if(bizTotal.compareTo(request.getPayAmount())!=0){
            log.error("验证价格失败{}",request);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }
}
