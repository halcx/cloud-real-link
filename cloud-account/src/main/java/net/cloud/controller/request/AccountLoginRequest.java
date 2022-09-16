package net.cloud.controller.request;

import lombok.Data;

/**
 * 登陆请求
 */
@Data
public class AccountLoginRequest {
    private String phone;
    private String pwd;
}
