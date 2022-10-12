package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.ShortLinkManager;
import net.cloud.mapper.ShortLinkMapper;
import net.cloud.model.ShortLinkDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShortLinkManagerImpl implements ShortLinkManager {

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Override
    public int addShortLink(ShortLinkDO shortLinkDO) {
        return shortLinkMapper.insert(shortLinkDO);
    }

    @Override
    public ShortLinkDO findByShortLinkCode(String shortLinkCode) {
        return shortLinkMapper.selectOne(new QueryWrapper<ShortLinkDO>().eq("code",shortLinkCode).eq("del",0));
    }

    /**
     * 删除短链
     * @param shortLinkDO
     * @return
     */
    @Override
    public int del(ShortLinkDO shortLinkDO) {
        int rows = shortLinkMapper.update(null, new UpdateWrapper<ShortLinkDO>()
                .eq("code", shortLinkDO.getCode())
                .eq("account_no",shortLinkDO.getAccountNo())
                .set("del",1));
        return rows;
    }

    @Override
    public int update(ShortLinkDO shortLinkDO) {
        int rows = shortLinkMapper.update(null, new UpdateWrapper<ShortLinkDO>().eq("code", shortLinkDO.getCode())
                .eq("del", 0)
                .eq("account_no",shortLinkDO.getAccountNo())
                .set("title", shortLinkDO.getTitle())
                .set("domain", shortLinkDO.getDomain()));
        return rows;
    }
}
