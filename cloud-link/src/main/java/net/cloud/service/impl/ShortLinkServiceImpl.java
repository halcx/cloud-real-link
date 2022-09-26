package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.ShortLinkManager;
import net.cloud.model.ShortLinkDO;
import net.cloud.service.ShortLinkService;
import net.cloud.vo.ShortLinkVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShortLinkServiceImpl implements ShortLinkService {

    @Autowired
    private ShortLinkManager shortLinkManager;
    /**
     * 解析短链
     * @param shortLinkCode
     * @return
     */
    @Override
    public ShortLinkVO parseShortLinkCode(String shortLinkCode) {
        ShortLinkDO byShortLinkCode = shortLinkManager.findByShortLinkCode(shortLinkCode);
        if(byShortLinkCode == null){
            return null;
        }
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        BeanUtils.copyProperties(byShortLinkCode,shortLinkVO);
        return shortLinkVO;
    }
}
