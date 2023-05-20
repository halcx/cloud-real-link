package net.cloud.service;

import net.cloud.controller.request.TrafficPageRequest;
import net.cloud.controller.request.UseTrafficRequest;
import net.cloud.model.EventMessage;
import net.cloud.utils.JsonData;
import net.cloud.vo.TrafficVO;

import java.util.Map;

public interface TrafficService {
    void handleTrafficMessage(EventMessage eventMessage);

    Map<String, Object> pageAvailable(TrafficPageRequest request);

    TrafficVO detail(Long trafficId);

    boolean deleteExpiredTraffic();

    JsonData reduce(UseTrafficRequest useTrafficRequest);
}
