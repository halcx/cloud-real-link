package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.ShopApplication;
import net.cloud.config.PayBeanConfig;
import net.cloud.config.WechatPayApi;
import net.cloud.config.WechatPayConfig;
import net.cloud.manager.ProductOrderManager;
import net.cloud.model.ProductOrderDO;
import net.cloud.utils.CommonUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)
@Slf4j
public class WechatPayTest {

    @Autowired
    private PayBeanConfig payBeanConfig;

    @Autowired
    private WechatPayConfig payConfig;

    @Autowired
    private CloseableHttpClient wechatPayClient;

    @Test
    public void testLoadPrivateKey() throws IOException {
        String algorithm = payBeanConfig.getPrivateKey().getAlgorithm();
        System.out.println(algorithm);
    }

    /**
     * {
     * 	"mchid": "1900006XXX",
     * 	"out_trade_no": "native12177525012014070332333",
     * 	"appid": "wxdace645e0bc2cXXX",
     * 	"description": "Image形象店-深圳腾大-QQ公仔",
     * 	"notify_url": "https://weixin.qq.com/",
     * 	"amount": {
     * 		"total": 1,
     * 		"currency": "CNY"
     *        }
     * }
     */
    @Test
    public void testWeChatPay() throws JSONException {
        String outTradeNo = CommonUtil.getStringNumRandom(32);
        JSONObject payObj = new JSONObject();
        payObj.put("mchid",payConfig.getMchId());
        payObj.put("out_trade_no",outTradeNo);
        payObj.put("appid",payConfig.getWxPayAppid());
        payObj.put("description","test");
        payObj.put("notify_url",payConfig.getCallbackUrl());

        JSONObject amountObj = new JSONObject();
        amountObj.put("total",100);
        amountObj.put("currency","CNY");

        payObj.put("amount",amountObj);
        //附属参数，可以用在回调
        payObj.put("attach","{\"accountNo\":"+888+"}");

        String body = payObj.toString();
        log.info("body:{}",body);

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(WechatPayApi.NATIVE_ORDER);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);

        try(CloseableHttpResponse closeableHttpResponse = wechatPayClient.execute(httpPost)) {
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            //响应体
            String responseStr = EntityUtils.toString(closeableHttpResponse.getEntity());

            log.info("下单响应码：{},响应体：{}",statusCode,responseStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWeChatQuery() {
        String outTradNo = "FW7xTBxN8TNp5gZa0g048mXIVgqppl9X";

        String url = String.format(WechatPayApi.NATIVE_QUERY, outTradNo, payConfig.getMchId());

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept","application/json");

        try(CloseableHttpResponse closeableHttpResponse = wechatPayClient.execute(httpGet)) {
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            //响应体
            String responseStr = EntityUtils.toString(closeableHttpResponse.getEntity());

            log.info("查询响应码：{},响应体：{}",statusCode,responseStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWeChatClose() throws JSONException {
        String outTradNo = "FW7xTBxN8TNp5gZa0g048mXIVgqppl9X";

        JSONObject payObj = new JSONObject();
        payObj.put("mchid",payConfig.getMchId());

        String body = payObj.toString();

        StringEntity entity = new StringEntity(body,"utf-8");
        entity.setContentType("application/json");

        String url = String.format(WechatPayApi.NATIVE_CLOSE, outTradNo);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);


        try(CloseableHttpResponse closeableHttpResponse = wechatPayClient.execute(httpPost)) {
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();

            log.info("关闭订单响应码：{}",statusCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWechatRefund(){

    }
}
