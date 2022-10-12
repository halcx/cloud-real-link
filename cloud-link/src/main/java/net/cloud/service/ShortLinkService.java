package net.cloud.service;

import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.controller.request.ShortLinkDelRequest;
import net.cloud.controller.request.ShortLinkPageRequest;
import net.cloud.controller.request.ShortLinkUpdateRequest;
import net.cloud.model.EventMessage;
import net.cloud.utils.JsonData;
import net.cloud.vo.ShortLinkVO;

import java.util.Map;

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

    Map<String, Object> pageByGroupId(ShortLinkPageRequest request);

    /**
     * 删除短链
     * @param request
     * @return
     */
    JsonData del(ShortLinkDelRequest request);

    JsonData update(ShortLinkUpdateRequest request);

    /**
     * 处理新增短链消息
     * @param eventMessage
     * @return
     */
    boolean handleAddShortLink(EventMessage eventMessage);

    /**
     * 处理更新短链消息
     * @param eventMessage
     * @return
     */
    boolean handleUpdateShortLink(EventMessage eventMessage);

    /**
     * 处理删除短链消息
     * @param eventMessage
     * @return
     */
    boolean handleDelShortLink(EventMessage eventMessage);
}
