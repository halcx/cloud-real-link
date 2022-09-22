package net.cloud.manager;

import net.cloud.model.ShortLinkDO;

public interface ShortLinkManager {
    /**
     * 新增短链
     * @param shortLinkDO
     * @return
     */
    int addShortLink(ShortLinkDO shortLinkDO);

    /**
     * 根据短链码查找短链
     * @param shortLinkCode
     * @return
     */
    ShortLinkDO findByShortLinkCode(String shortLinkCode);

    /**
     * 删除短链
     * @param shortLinkCode
     * @param accountNo
     * @return
     */
    int del(String shortLinkCode,Long accountNo);
}
