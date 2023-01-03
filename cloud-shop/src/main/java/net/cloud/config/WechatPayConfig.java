package net.cloud.config;


import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "pay.wechat")
public class WechatPayConfig {
    /**
     * 商户号
     */
    private String mchId;
    /**
     * 公众号id
     */
    private String wxPayAppid;
    /**
     * 商户证书序列号,需要和证书对应
     */
    private String mchSerialNo;
    /**
     * api密钥
     */
    private String apiV3Key;
    /**
     * 商户私钥路径（微信服务端会根据证书序列号，找到证书获取公钥进⾏解密数据）
     */
    private String privateKeyPath;
    /**
     * ⽀付成功⻚⾯跳转
     */
    private String successReturnUrl;
    /**
     * ⽀付成功，回调通知
     */
    private String callbackUrl;

    public static class Url{
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
    }
}
