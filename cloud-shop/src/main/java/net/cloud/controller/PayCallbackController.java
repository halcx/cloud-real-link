package net.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import net.cloud.config.WechatPayConfig;
import net.cloud.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/api/callback/order/v1")
@Slf4j
public class PayCallbackController {
    @Autowired
    private WechatPayConfig wechatPayConfig;

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private ScheduledUpdateCertificatesVerifier verifier;


    /**
     * 获取报文
     * 验证签名
     * 解密
     * 处理业务逻辑
     * 响应请求
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("wechat")
    public Map<String,String> wechatPayCallback(HttpServletResponse response, HttpServletRequest request) throws IOException {

        //随机串
        String wechatPayNonce = request.getHeader("Wechatpay-Nonce");
        String requestID = request.getHeader("Request-ID");
        //微信传过来的签名
        String wechatPaySignature = request.getHeader("Wechatpay-Signature");
        //证书序列号
        String wechatPaySerial = request.getHeader("Wechatpay-Serial");
        //时间戳
        String wechatPayTimestamp = request.getHeader("Wechatpay-Timestamp");
        String requestBody = getRequestBody(request);
        String signStr = Stream.of(wechatPayTimestamp,wechatPayNonce,requestBody).collect(Collectors.joining("\n","","\n"));
        HashMap<String, String> map = new HashMap<>(2);
        try {
            //验证签名是否通过
            boolean result = verifySign(wechatPaySerial,signStr,wechatPaySignature);
            if(result){
                //TODO 解密数据
                String plainBody = decryptBody(requestBody);
                log.info("解密后的明文:{}",plainBody);
                //转换成map
                Map<String, String> paramsMap = convertWechatPayMsgToMap(plainBody);
                //TODO 处理业务逻辑

                //响应微信
                map.put("code","SUCCESS");
                map.put("message","成功");
            }


        }catch (Exception e){
            log.error("微信支付回调异常:{}",e);
        }
        return map;
    }

    /**
     * 转换body为map
     * @param plainBody
     * @return
     */
    private Map<String,String> convertWechatPayMsgToMap(String plainBody){
        Map<String,String> paramsMap = new HashMap<>(2);
        JSONObject jsonObject = JSONObject.parseObject(plainBody);
        //商户订单号
        paramsMap.put("out_trade_no",jsonObject.getString("out_trade_no"));
        //交易状态
        paramsMap.put("trade_status",jsonObject.getString("trade_status"));
        //附加数据
        paramsMap.put("account_no",jsonObject.getJSONObject("attach").getString("accountNo"));
        return paramsMap;
    }

    /**
     * 解密body
     * @param requestBody
     * @return
     */
    private String decryptBody(String requestBody) throws GeneralSecurityException {
        AesUtil aesUtil = new AesUtil(wechatPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        JSONObject obj = JSONObject.parseObject(requestBody);
        JSONObject resource = obj.getJSONObject("resource");
//        "resource": {
//            "original_type": "transaction",
//                    "algorithm": "AEAD_AES_256_GCM",
//                    "ciphertext": "",
//                    "associated_data": "",
//                    "nonce": ""
//        }
        String ciphertext = resource.getString("ciphertext");
        String associatedData = resource.getString("associated_data");
        String nonce = resource.getString("nonce");
        String body = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
        return body;
    }

    /**
     * 验证签名
     * @param serialNo 微信平台-证书序列号
     * @param signStr 自己组装的签名
     * @param wechatPaySignature 微信的签名
     * @return
     */
    private boolean verifySign(String serialNo, String signStr,String wechatPaySignature ){
        return verifier.verify(serialNo,signStr.getBytes(StandardCharsets.UTF_8),wechatPaySignature);
    }

    /**
     * 读取请求数据流
     * @param request
     * @return
     * @throws IOException
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuffer sb = new StringBuffer();

        try(ServletInputStream inputStream = request.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String line;
            while ((line = reader.readLine())!=null){
                sb.append(line);
            }
        }catch (IOException e){
            log.error("读取数据流异常:{}",e);
        }
        return sb.toString();
    }
}
