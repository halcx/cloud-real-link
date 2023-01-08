package net.cloud.component;

import net.cloud.vo.PayInfoVO;
import org.json.JSONException;

public class PayStrategyContext {

    private PayStrategy payStrategy;

    public PayStrategyContext(PayStrategy payStrategy){
        this.payStrategy = payStrategy;
    }

    /**
     * 根据策略执行不同的下单接口
     * @param payInfoVO
     * @return
     */
    public String executeUnifiedOrder(PayInfoVO payInfoVO) throws JSONException {
        return payStrategy.unifiedOrder(payInfoVO);
    }

    /**
     * 根据策略执行不同的退款接口
     * @param payInfoVO
     * @return
     */
    public String executeRefund(PayInfoVO payInfoVO){
        return payStrategy.refund(payInfoVO);
    }

    /**
     * 根据策略执行不同的关单接口
     * @param payInfoVO
     * @return
     */
    public String executeCloseOrder(PayInfoVO payInfoVO){
        return payStrategy.closeOrder(payInfoVO);
    }

    /**
     * 根据策略执行不同的查询接口
     * @param payInfoVO
     * @return
     */
    public String executeQueryPayStatus(PayInfoVO payInfoVO){
        return payStrategy.queryPayState(payInfoVO);
    }
}
