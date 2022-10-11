package net.cloud.controller.request;

import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;

@Data
public class ShortLinkDelRequest {
    /**
     * 组
     */
    private Long groupId;

    /**
     * 映射id
     */
    private Long mappingId;

    /**
     * 短链码
     */
    private String code;
}
