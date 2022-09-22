package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        return shortLinkMapper.selectOne(new QueryWrapper<ShortLinkDO>().eq("code",shortLinkCode));
    }

    /**
     * 删除是逻辑删除
     * @param shortLinkCode
     * @param accountNo
     * @return
     */
    @Override
    public int del(String shortLinkCode, Long accountNo) {
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDel(1);
        int rows = shortLinkMapper.update(shortLinkDO, new QueryWrapper<ShortLinkDO>()
                .eq("code", shortLinkCode)
                .eq("account_no", accountNo));
        return rows;
    }
}
