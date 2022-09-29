package net.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage implements Serializable {
    /**
     * 消息队列的消息id，用于幂等性处理
     */
    private String messageId;

    /**
     * 事件类型
     */
    private String eventMessageType;

    /**
     * 业务id
     */
    private String bizId;

    /**
     * 账号
     */
    private Long accountNo;

    /**
     * 消息体，用json序列化的形式
     */
    private String content;

    /**
     * 备注
     */
    private String remark;
}
