package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.component.ShortLinkComponent;
import net.cloud.config.RabbitMQConfig;
import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.controller.request.ShortLinkDelRequest;
import net.cloud.controller.request.ShortLinkPageRequest;
import net.cloud.controller.request.ShortLinkUpdateRequest;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

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
    public boolean handleAddShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String eventMessageType = eventMessage.getEventMessageType();
        //需要把类型转换一下，因为前面给它序列化成为字符串了
        ShortLinkAddRequest shortLinkAddRequest = JsonUtil.json2Obj(eventMessage.getContent(),ShortLinkAddRequest.class);
        //短链域名校验
        DomainDO domainDO = checkDomain(shortLinkAddRequest.getDomainType(),shortLinkAddRequest.getDomainId(),accountNo);
        //校验组是否合法
        LinkGroupDO linkGroupDO = checkLinkGroup(shortLinkAddRequest.getGroupId(), accountNo);

        //短链码重复标记
        boolean duplicateCodeFlag = false;

        //长链摘要生成
        String originalUrlDigest = CommonUtil.MD5(shortLinkAddRequest.getOriginalUrl());

        //生成短链码
        String shortLinkCode = shortLinkComponent.createShortLinkCode(shortLinkAddRequest.getOriginalUrl());

        //加锁
        //key1是短链码，ARGV[1]是accountNo,ARGV[2]是过期时间
        String script = "if redis.call('EXISTS',KEYS[1])==0 " +
                "then redis.call('set',KEYS[1],ARGV[1]); " +
                "redis.call('expire',KEYS[1],ARGV[2]); " +
                "return 1;" +
                " elseif redis.call('get',KEYS[1]) == ARGV[1] " +
                "then return 2;" +
                " else return 0; " +
                "end;";

        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(shortLinkCode), accountNo,100);

        //加锁成功
        if(result>0){
            if(EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(eventMessageType)){
                //C 端处理

                //在数据库里面是否被占用，如果不为空，则已经被占用了
                ShortLinkDO shortLinkCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);
                if(shortLinkCodeDOInDB==null){
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
                }else {
                    //不为空就重复了
                    log.error("C端短链码重复：{}",eventMessage);
                    duplicateCodeFlag = true;
                }


            }else if(EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(eventMessageType)){

                //查找短链码是否存在
                GroupCodeMappingDO groupCodeMappingDOInDB = groupCodeMappingManager.findByCodeAndGroupId(shortLinkCode,linkGroupDO.getId(),accountNo);

                if(groupCodeMappingDOInDB==null){
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
                }else {
                    //不为空就重复了
                    log.error("B端短链码重复：{}",eventMessage);
                    duplicateCodeFlag = true;
                }
            }
        }else {
            //加锁失败，自旋100ms，再调用
            //失败的原因可能是短链码已经被占用了，需要重新生成
            log.error("加锁失败：{}",eventMessage);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            duplicateCodeFlag = true;
        }

        if(duplicateCodeFlag){
            //需要重新生成短链，版本号递增
            String newOriginalVersion = CommonUtil.addUrlPrefixVersion(shortLinkAddRequest.getOriginalUrl());
            shortLinkAddRequest.setOriginalUrl(newOriginalVersion);
            eventMessage.setContent(JsonUtil.obj2Json(shortLinkAddRequest));
            log.warn("短链码保存失败，重新生成:{}",eventMessage);
            //递归地调用这个方法
            handleAddShortLink(eventMessage);
        }

        return false;
    }

    @Override
    public boolean handleUpdateShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkUpdateRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkUpdateRequest.class);

        //校验域名是否合法
        DomainDO domainDO = checkDomain(request.getDomainType(), request.getDomainId(), accountNo);

        if(EventMessageType.SHORT_LINK_UPDATE_LINK.name().equalsIgnoreCase(messageType)){
            //C端

            //如果传入参数过多的话，最好用一个对象进行封装
            //C端只需要短链码就可以进入对应的库表
            ShortLinkDO shortLinkDO = ShortLinkDO.builder().code(request.getCode()).title(request.getTitle()).domain(domainDO.getValue()).build();

            int rows = shortLinkManager.update(shortLinkDO);
            log.debug("更新C端短链，rows={}",rows);
            return true;
        }else if (EventMessageType.SHORT_LINK_UPDATE_MAPPING.name().equalsIgnoreCase(messageType)){
            //B端
            //B端需要account_no和group_id
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder().id(request.getMappingId()).groupId(request.getGroupId()).accountNo(accountNo)
                    .title(request.getTitle()).domain(domainDO.getValue()).build();
            int rows = groupCodeMappingManager.update(groupCodeMappingDO);
            log.debug("更新B端短链，rows={}",rows);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleDelShortLink(EventMessage eventMessage) {
        return false;
    }

    /**
     * 分页查找
     * 从B端查找，group_code_mapping
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> pageByGroupId(ShortLinkPageRequest request) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map<String, Object> result = groupCodeMappingManager.pageShortLinkByGroupId(request.getPage(), request.getSize(), accountNo, request.getGroupId());
        return result;
    }

    @Override
    public JsonData update(ShortLinkUpdateRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        EventMessage eventMessage = EventMessage.builder()
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_UPDATE.name())
                .build();

        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkUpdateRoutingKey(),eventMessage);

        return JsonData.buildSuccess();
    }

    @Override
    public JsonData del(ShortLinkDelRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        EventMessage eventMessage = EventMessage.builder()
                .accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_DEL.name())
                .build();

        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkDelRoutingKey(),eventMessage);

        return JsonData.buildSuccess();
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
