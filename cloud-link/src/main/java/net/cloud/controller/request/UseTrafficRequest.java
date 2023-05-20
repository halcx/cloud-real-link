package net.cloud.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
