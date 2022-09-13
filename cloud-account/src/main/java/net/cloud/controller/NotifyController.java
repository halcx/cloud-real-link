package net.cloud.controller;

import net.cloud.service.NotifyService;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account/v1/")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    /**
     * 测试发送验证码的接口，主要用于对比优化前后区别
     * @return
     */
    @RequestMapping("send_code")
    public JsonData sendCode(){

        return JsonData.buildSuccess();
    }

}
