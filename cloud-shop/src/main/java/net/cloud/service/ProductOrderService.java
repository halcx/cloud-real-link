package net.cloud.service;

import net.cloud.controller.request.ConfirmOrderRequest;
import net.cloud.utils.JsonData;

import java.util.Map;

public interface ProductOrderService {
    Map<String, Object> page(int page, int size, String state);

    String queryProductOrderState(String outTradeNo);

    JsonData confirmOrder(ConfirmOrderRequest request);
}
