package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.component.SmsComponent;
import net.cloud.config.SmsConfig;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.SendCodeEnum;
import net.cloud.service.NotifyService;
import net.cloud.utils.CheckUtil;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        //生成一个六位数的验证码
        String code = CommonUtil.getRandomCode(6);

        //TODO 重复发送等判断逻辑

        if(CheckUtil.isPhone(to)){
            //发送手机验证码
            smsComponent.send(to,smsConfig.getTemplateId(),code);

        }else if(CheckUtil.isEmail(to)){
            //TODO 发送邮箱验证码
        }
        return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
    }
}
