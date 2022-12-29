package net.cloud.controller.request;

import lombok.Data;

@Data
public class ProductOrderPageRequest {
    /**
     * 状态
     */
    private String state;

    /**
     * 第几页
     */
    private int page;

    /**
     * 每页多少条
     */
    private int size;
}
