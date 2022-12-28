package net.cloud.controller;


import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.ConfirmOrderRequest;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.ClientTypeEnum;
import net.cloud.enums.ProductOrderPayEnum;
import net.cloud.service.ProductOrderService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Wxh
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/api/order/v1")
@Slf4j
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @GetMapping("page")
    public JsonData page(
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value = "state",required = false) String state
    ){
        Map<String,Object> pageResult = productOrderService.page(page,size,state);
        return JsonData.buildSuccess(pageResult);
    }

    /**
     * 想一下这种场景，我们怎么会知道用户到底支付了没有呢
     * 实际上可以通过后台不断轮询订单状态，去判断，这个时候就需要查询订单状态了
     * @param outTradeNo
     * @return
     */
    @GetMapping("query_state")
    public JsonData queryState(
            @RequestParam(value = "out_trade_no") String outTradeNo
    ){
        String state = productOrderService.queryProductOrderState(outTradeNo);
        return StringUtils.isBlank(state) ? JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST) : JsonData.buildSuccess(state);
    }

    /**
     * 下单接口
     * @param request
     * @param response
     */
    @PostMapping("confirm")
    public void confirmOrder(@RequestBody ConfirmOrderRequest request, HttpServletResponse response){
        JsonData jsonData = productOrderService.confirmOrder(request);
        if(jsonData.getCode()==0){
            //端类型
            String clientType = request.getClientType();
            //支付类型
            String payType = request.getPayType();
            //如果是支付宝支付的话就要跳转到相关网页
            if(payType.equalsIgnoreCase(ProductOrderPayEnum.ALI_PAY.name())){
                if(clientType.equalsIgnoreCase(ClientTypeEnum.PC.name())){
                    CommonUtil.sendHtmlMessage(response,jsonData);
                }else if(clientType.equalsIgnoreCase(ClientTypeEnum.APP.name())){

                }else if(clientType.equalsIgnoreCase(ClientTypeEnum.H5.name())){

                }
            }else if (payType.equalsIgnoreCase(ProductOrderPayEnum.WECHAT_PAY.name())){
                //wechat支付
                CommonUtil.sendJsonMessage(response,jsonData);
            }

        }else {
            log.error("创建订单失败{}",jsonData.toString());
            CommonUtil.sendJsonMessage(response,jsonData);
        }
    }
}

