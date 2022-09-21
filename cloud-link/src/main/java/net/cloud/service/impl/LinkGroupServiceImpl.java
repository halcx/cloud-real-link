package net.cloud.service.impl;

import net.cloud.controller.request.LinkGroupAddRequest;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.LinkGroupManager;
import net.cloud.model.LinkGroupDO;
import net.cloud.service.LinkGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkGroupServiceImpl implements LinkGroupService {

    @Autowired
    private LinkGroupManager linkGroupManager;

    /**
     * 增加分组
     * @param addRequest
     * @return
     */
    @Override
    public int add(LinkGroupAddRequest addRequest) {

        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(addRequest.getTitle());
        linkGroupDO.setAccountNo(accountNo);

        int rows = linkGroupManager.add(linkGroupDO);
        return rows;
    }

    /**
     * 删除分组
     * @param groupId
     * @return
     */
    @Override
    public int del(Long groupId) {
        //删除的时候一定要获取已经登陆的account_no，防止越权
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        int rows = linkGroupManager.del(groupId,accountNo);
        return rows;
    }
}
