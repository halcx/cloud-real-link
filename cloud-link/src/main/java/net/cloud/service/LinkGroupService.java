package net.cloud.service;

import net.cloud.controller.request.LinkGroupAddRequest;

public interface LinkGroupService {
    /**
     * 新增分组
     * @param addRequest
     * @return
     */
    int add(LinkGroupAddRequest addRequest);

    int del(Long groupId);
}
