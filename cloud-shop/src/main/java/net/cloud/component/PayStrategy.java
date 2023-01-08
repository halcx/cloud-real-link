package net.cloud.component;

import net.cloud.vo.PayInfoVO;
import org.json.JSONException;

public interface PayStrategy {
    /**
     * 统一下单接口
     * @param payInfoVO
     * @return
     */
    String unifiedOrder(PayInfoVO payInfoVO) throws JSONException;

    /**
     * 退款接口
     * @param payInfoVO
     * @return
     */
    default String refund(PayInfoVO payInfoVO){return "";}

    /**
     * 查询支付状态接口
     * @param payInfoVO
     * @return
     */
    default String queryPayState(PayInfoVO payInfoVO){return "";}

    /**
     * 关闭订单接口
     * @param payInfoVO
     * @return
     */
    default String closeOrder(PayInfoVO payInfoVO){return "";}
}
