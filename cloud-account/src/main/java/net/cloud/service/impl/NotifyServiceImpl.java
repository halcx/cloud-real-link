package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.component.SmsComponent;
import net.cloud.config.SmsConfig;
import net.cloud.constant.RedisKey;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.SendCodeEnum;
import net.cloud.service.NotifyService;
import net.cloud.utils.CheckUtil;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    /**
     * 验证码10min有效
     */
    private static final int CODE_EXPIRED = 60*1000*10;

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;



    /**
     * 发送验证码接口
     * 发送了验证码之后60s之后才能再发送，发送的验证码10min有效
     *
     * 前置操作：判断是否重复发送
     * 1、存储验证码到缓存
     * 2、发送短信验证码
     * 后置操作：存储发送记录(或者直接log)
     * @param sendCodeEnum
     * @param to
     * @return
     */
    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        /**
         * 短信验证码防刷方案
         * 1、前端增加校验倒计时，不到60s按钮不给点击
         * 2、增加redis存储，发送的时候设置一下额外的key，并且60s之后过期：
         *      1 非原子操作
         *      2 增加额外的kv，浪费空间
         * 3、基于原先的key拼装时间戳（用这种方式）
         *      满足了当前节点内的原执行，也满足业务需求
         *      key:手机号    value:code_时间戳
         */

        String cacheKey = String.format(RedisKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);

        //拿到缓存中的key
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        //如果不为null，再判断是否是60s内重复发送 code_时间戳
        if(StringUtils.isNotBlank(cacheValue)){
            long ttl = Long.parseLong(cacheValue.split("_")[1]);
            long leftTime = CommonUtil.getCurrentTimestamp() - ttl;
            //当前时间戳-验证码发送时间戳 如果小于60s则不给发送
            if(leftTime < (1000*60)){
                log.info("重复发送短信验证码，时间间隔:{}秒",leftTime);
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }

        //生成和拼接好验证码
        //1、生成一个六位数的验证码
        String code = CommonUtil.getRandomCode(6);
        //2、拼接
        String value = code+"_"+CommonUtil.getCurrentTimestamp();
        //存到redis
        redisTemplate.opsForValue().set(cacheKey,value,CODE_EXPIRED,TimeUnit.MILLISECONDS);

        if(CheckUtil.isPhone(to)){
            //发送手机验证码
            smsComponent.send(to,smsConfig.getTemplateId(),code);

        }else if(CheckUtil.isEmail(to)){
            //TODO 发送邮箱验证码
        }
        return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
    }
}
