package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.component.ShortLinkComponent;
import net.cloud.config.RabbitMQConfig;
import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.enums.DomainTypeEnum;
import net.cloud.enums.EventMessageType;
import net.cloud.enums.ShortLinkStateEnum;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.DomainManager;
import net.cloud.manager.GroupCodeMappingManager;
import net.cloud.manager.LinkGroupManager;
import net.cloud.manager.ShortLinkManager;
import net.cloud.model.*;
import net.cloud.service.ShortLinkService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.IDUtil;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import net.cloud.vo.ShortLinkVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class ShortLinkServiceImpl implements ShortLinkService {

    @Autowired
    private ShortLinkManager shortLinkManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private DomainManager domainManager;

    @Autowired
    private LinkGroupManager linkGroupManager;

    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Autowired
    private GroupCodeMappingManager groupCodeMappingManager;

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

    /**
     * 创建短链 这个就是生产者
     * @param request
     * @return
     */
    @Override
    public JsonData createShortLink(ShortLinkAddRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);

        EventMessage eventMessage = EventMessage.builder()
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
        return JsonData.buildSuccess();
    }

    /**
     * 短链新增逻辑 C端
     *
     * 1、判断短链域名是否合法
     * 2、判断著名是否合法
     * 3、生成长链摘要
     * 4、生成短链码
     * 5、加锁
     * 6、查询短链码是否存在
     * 7、构建短链对象
     * 8、保存数据库
     * @param eventMessage
     * @return
     */
    @Override
    public boolean handlerAddShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String eventMessageType = eventMessage.getEventMessageType();
        //需要把类型转换一下，因为前面给它序列化成为字符串了
        ShortLinkAddRequest shortLinkAddRequest = JsonUtil.json2Obj(eventMessage.getContent(),ShortLinkAddRequest.class);
        //短链域名校验
        DomainDO domainDO = checkDomain(shortLinkAddRequest.getDomainType(),shortLinkAddRequest.getDomainId(),accountNo);
        //校验组是否合法
        LinkGroupDO linkGroupDO = checkLinkGroup(shortLinkAddRequest.getGroupId(), accountNo);

        //长链摘要生成
        String originalUrlDigest = CommonUtil.MD5(shortLinkAddRequest.getOriginalUrl());

        //生成短链码
        String shortLinkCode = shortLinkComponent.createShortLinkCode(shortLinkAddRequest.getOriginalUrl());

        //TODO 加锁

        //在数据库里面是否被占用，如果不为空，则已经被占用了
        ShortLinkDO shortLinkCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);

        if(shortLinkCodeDOInDB == null){
            if(EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(eventMessageType)){
                //C 端处理
                //存储短链码
                ShortLinkDO shorLinkDo = ShortLinkDO.builder()
                        .accountNo(accountNo)
                        .code(shortLinkCode)
                        .title(shortLinkAddRequest.getTitle())
                        .originalUrl(shortLinkAddRequest.getOriginalUrl())
                        .domain(domainDO.getValue())
                        .groupId(linkGroupDO.getId())
                        .expired(shortLinkAddRequest.getExpired())
                        .sign(originalUrlDigest)
                        .state(ShortLinkStateEnum.ACTIVE.name())
                        .del(0)
                        .build();

                shortLinkManager.addShortLink(shorLinkDo);
                return true;
            }else if(EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(eventMessageType)){
                //B 端处理
                GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                        .accountNo(accountNo)
                        .code(shortLinkCode)
                        .title(shortLinkAddRequest.getTitle())
                        .originalUrl(shortLinkAddRequest.getOriginalUrl())
                        .domain(domainDO.getValue())
                        .groupId(linkGroupDO.getId())
                        .expired(shortLinkAddRequest.getExpired())
                        .sign(originalUrlDigest)
                        .state(ShortLinkStateEnum.ACTIVE.name())
                        .del(0)
                        .build();
                groupCodeMappingManager.add(groupCodeMappingDO);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断域名是否合法
     * @param domainType
     * @param domainId
     * @param accountNo
     * @return
     */
    private DomainDO checkDomain(String domainType,Long domainId,Long accountNo){
        DomainDO domainDO;

        //判断域名是否为自定义的
        if(DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)){
            //根据域名id和账号去找一下域名
            domainDO = domainManager.findById(domainId, accountNo);
        }else {
            domainDO = domainManager.findByDomainTypeAndId(domainId,DomainTypeEnum.OFFICIAL);
        }

        Assert.notNull(domainDO,"域名不合法");
        return domainDO;
    }

    /**
     * 校验组是否合法
     * @param groupId
     * @param accountNo
     * @return
     */
    private LinkGroupDO checkLinkGroup(Long groupId,Long accountNo){
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        Assert.notNull(linkGroupDO,"组名不合法");
        return linkGroupDO;
    }
}
