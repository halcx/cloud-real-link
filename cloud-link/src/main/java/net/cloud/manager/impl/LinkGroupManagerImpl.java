package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.cloud.manager.LinkGroupManager;
import net.cloud.mapper.LinkGroupMapper;
import net.cloud.model.LinkGroupDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public LinkGroupDO detail(Long groupId, long accountNo) {
        return linkGroupMapper.selectOne(new QueryWrapper<LinkGroupDO>().eq("id",groupId).eq("account_no",accountNo));
    }

    @Override
    public List<LinkGroupDO> listAllGroup(long accountNo) {
        return linkGroupMapper.selectList(new QueryWrapper<LinkGroupDO>().eq("account_no",accountNo));
    }

    @Override
    public int updateById(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.update(linkGroupDO,new QueryWrapper<LinkGroupDO>()
                .eq("id",linkGroupDO.getId()).eq("account_no",linkGroupDO.getAccountNo()));
    }
}
