package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.service.LogService;
import net.cloud.utils.JsonData;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogServiceImpl implements LogService {
    @Override
    public JsonData recordShortLinkLog(String message) {
        log.info("这个是记录日志:{}",message);
        return JsonData.buildSuccess();
    }
}
