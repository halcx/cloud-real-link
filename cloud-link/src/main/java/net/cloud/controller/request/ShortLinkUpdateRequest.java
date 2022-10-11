package net.cloud.controller.request;

import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;

@Data
public class ShortLinkUpdateRequest {
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

    /**
     * 更新的标题
     */
    private String title;

    /**
     * 更新的域名
     */
    private Long domainId;

    /**
     * 更新的域名类型
     */
    private String domainType;
}
