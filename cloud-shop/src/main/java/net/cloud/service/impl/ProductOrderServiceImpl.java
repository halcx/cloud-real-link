package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.constant.TimeConstant;
import net.cloud.controller.request.ConfirmOrderRequest;
import net.cloud.enums.BillTypeEnum;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.ProductOrderPayEnum;
import net.cloud.enums.ProductOrderStateEnum;
import net.cloud.exception.BizException;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.ProductManager;
import net.cloud.manager.ProductOrderManager;
import net.cloud.model.LoginUser;
import net.cloud.model.ProductDO;
import net.cloud.model.ProductOrderDO;
import net.cloud.service.ProductOrderService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import net.cloud.vo.PayInfoVO;
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

    @Override
    public Map<String, Object> page(int page, int size, String state) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map<String, Object> pageResult = productOrderManager.page(page, size, accountNo, state);
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

        //TODO 发送延迟消息 用于关单

        //TODO 调用支付信息

        return null;
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
