package net.cloud.service;

import net.cloud.utils.JsonData;
import org.apache.kafka.common.protocol.types.Field;

public interface LogService {
    JsonData recordShortLinkLog(String message);
}
