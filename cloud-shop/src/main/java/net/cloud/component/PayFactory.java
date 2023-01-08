package net.cloud.component;

import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.ProductOrderPayEnum;
import net.cloud.enums.ProductOrderStateEnum;
import net.cloud.vo.PayInfoVO;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayFactory {

    @Autowired
    private AliPayStrategy aliPayStrategy;

    @Autowired
    private WechatPayStrategy wechatPayStrategy;

    /**
     * 创建支付，简单工厂
     * @param payInfoVO
     * @return
     */
    public String pay(PayInfoVO payInfoVO) throws JSONException {
        String payType = payInfoVO.getPayType();
        if(ProductOrderPayEnum.ALI_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            //支付宝支付
            return payStrategyContext.executeUnifiedOrder(payInfoVO);
        }else if(ProductOrderPayEnum.WECHAT_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            //支付宝支付
            return payStrategyContext.executeUnifiedOrder(payInfoVO);
        }
        return "";
    }

    /**
     * 关闭订单，简单工厂
     * @param payInfoVO
     * @return
     */
    public String closeOrder(PayInfoVO payInfoVO){
        String payType = payInfoVO.getPayType();
        if(ProductOrderPayEnum.ALI_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            //支付宝支付
            return payStrategyContext.executeCloseOrder(payInfoVO);
        }else if(ProductOrderPayEnum.WECHAT_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            //支付宝支付
            return payStrategyContext.executeCloseOrder(payInfoVO);
        }
        return "";
    }

    /**
     * 查询支付状态，简单工厂
     * @param payInfoVO
     * @return
     */
    public String queryPayStatus(PayInfoVO payInfoVO){
        String payType = payInfoVO.getPayType();
        if(ProductOrderPayEnum.ALI_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            //支付宝支付
            return payStrategyContext.executeQueryPayStatus(payInfoVO);
        }else if(ProductOrderPayEnum.WECHAT_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            //支付宝支付
            return payStrategyContext.executeQueryPayStatus(payInfoVO);
        }
        return "";
    }

    /**
     * 退款，简单工厂
     * @param payInfoVO
     * @return
     */
    public String refund(PayInfoVO payInfoVO){
        String payType = payInfoVO.getPayType();
        if(ProductOrderPayEnum.ALI_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            //支付宝支付
            return payStrategyContext.executeRefund(payInfoVO);
        }else if(ProductOrderPayEnum.WECHAT_PAY.name().equalsIgnoreCase(payType)){
            //微信支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            //支付宝支付
            return payStrategyContext.executeRefund(payInfoVO);
        }
        return "";
    }
}
