package net.cloud.controller.request;

import lombok.Builder;
import lombok.Data;

@Data
public class UseTrafficRequest {
    /**
     * 账号
     */
    private Long accountNo;

    /**
     * 业务id，唯一标识
     */
    private String bizId;

}
