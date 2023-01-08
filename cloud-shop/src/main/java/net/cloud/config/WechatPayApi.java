package net.cloud.config;

public class WechatPayApi {
    /**
     * 微信支付主机地址
     */
    public static final String host = "https://api.mch.weixin.qq.com";

    /**
     * native下单接口
     */
    public static final String NATIVE_ORDER="https://api.mch.weixin.qq.com/v3/pay/transactions/native";

    public static final String NATIVE_ORDER_PATH="/v3/pay/transactions/native";

    /**
     * native订单查询接口，根据商户订单号查询
     */
    public static final String NATIVE_QUERY="https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s?mchid=%s";

    public static final String NATIVE_QUERY_PATH="/v3/pay/transactions/out-trade-no/%s?mchid=%s";

    /**
     * native关单接口
     */
    public static final String NATIVE_CLOSE="https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s/close";

    public static final String NATIVE_CLOSE_PATH="/v3/pay/transactions/out-trade-no/%s/close";

    /**
     * native申请退款接口
     */
    public static final String NATIVE_REFUND="https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    public static final String NATIVE_REFUND_PATH="/v3/refund/domestic/refunds";
}
