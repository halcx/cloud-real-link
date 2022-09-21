package net.cloud.controller.request;

import lombok.Data;

@Data
public class LinkGroupUpdateRequest {
    /**
     * 组的id
     */
    private Long id;
    /**
     * 组名
     */
    private String title;
}
