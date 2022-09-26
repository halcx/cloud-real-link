package net.cloud.service;

import net.cloud.vo.ShortLinkVO;

public interface ShortLinkService {
    /**
     * 解析短链
     * @param shortLinkCode
     * @return
     */
    ShortLinkVO parseShortLinkCode(String shortLinkCode);
}
