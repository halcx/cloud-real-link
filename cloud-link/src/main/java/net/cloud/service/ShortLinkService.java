package net.cloud.service;

import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.utils.JsonData;
import net.cloud.vo.ShortLinkVO;

public interface ShortLinkService {
    /**
     * 解析短链
     * @param shortLinkCode
     * @return
     */
    ShortLinkVO parseShortLinkCode(String shortLinkCode);

    /**
     * 创建短链
     * @param request
     * @return
     */
    JsonData createShortLink(ShortLinkAddRequest request);
}
