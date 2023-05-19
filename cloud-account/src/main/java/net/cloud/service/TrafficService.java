package net.cloud.service;

import net.cloud.controller.request.TrafficPageRequest;
import net.cloud.model.EventMessage;
import net.cloud.vo.TrafficVO;

import java.util.Map;

public interface TrafficService {
    void handleTrafficMessage(EventMessage eventMessage);

    Map<String, Object> pageAvailable(TrafficPageRequest request);

    TrafficVO detail(Long trafficId);

    boolean deleteExpiredTraffic();
}
