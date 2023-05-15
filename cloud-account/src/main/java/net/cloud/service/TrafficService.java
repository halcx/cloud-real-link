package net.cloud.service;

import net.cloud.model.EventMessage;

public interface TrafficService {
    void handleTrafficMessage(EventMessage eventMessage);
}
