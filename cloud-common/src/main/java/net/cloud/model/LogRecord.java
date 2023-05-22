package net.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogRecord {
    /**
     * 客户端ip
     */
    private String ip;

    /**
     * 访问的时间戳
     */
    private Long ts;

    /**
     * 日志事件类型
     */
    private String event;

    /**
     * udid是设备的唯一标识
     */
    private String udid;

    /**
     * 业务id
     */
    private String bizId;

    /**
     * 日志详情
     */
    private Object data;
}
