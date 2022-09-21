package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.cloud.manager.LinkGroupManager;
import net.cloud.mapper.LinkGroupMapper;
import net.cloud.model.LinkGroupDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkGroupManagerImpl implements LinkGroupManager {

    @Autowired
    private LinkGroupMapper linkGroupMapper;

    @Override
    public int add(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.insert(linkGroupDO);
    }

    @Override
    public int del(Long groupId, long accountNo) {
        return linkGroupMapper.delete(new QueryWrapper<LinkGroupDO>().eq("id",groupId).eq("account_no",accountNo));
    }
}
