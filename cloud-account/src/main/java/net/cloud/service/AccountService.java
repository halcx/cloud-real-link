package net.cloud.service;

import net.cloud.controller.request.AccountRegisterRequest;
import net.cloud.utils.JsonData;

public interface AccountService {
    /**
     * 用户注册
     * @param registerRequest
     * @return
     */
    JsonData register(AccountRegisterRequest registerRequest);
}
