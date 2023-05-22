package net.cloud.service;

import net.cloud.utils.JsonData;
import org.apache.kafka.common.protocol.types.Field;

import javax.servlet.http.HttpServletRequest;

public interface LogService {
    /**
     * 记录短链码的日志
     * @param request
     * @param shortLinkCode
     * @param accountNo
     * @return
     */
    void recordShortLinkLog(HttpServletRequest request,String shortLinkCode,Long accountNo);
}
