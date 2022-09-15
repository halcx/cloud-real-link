package net.cloud.service;

import net.cloud.enums.SendCodeEnum;
import net.cloud.utils.JsonData;

public interface NotifyService {

    /**
     * 发送短信验证码
     * @param sendCodeEnum
     * @param to
     * @return
     */
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);
}
