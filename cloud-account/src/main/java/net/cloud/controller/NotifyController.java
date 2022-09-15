package net.cloud.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.SendCodeRequest;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.SendCodeEnum;
import net.cloud.service.NotifyService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/account/v1/")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private Producer captchaProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证码过期时间
     */
    private static final long CAPTCHA_CODE_EXPIRED = 60*1000*10;

    /**
     * 生成验证码
     * @param request
     * @param response
     */
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        String captchaProducerText = captchaProducer.createText();
        log.info("验证码内容:{}",captchaProducerText);

        //存储到redis里面，配置过期时间
        redisTemplate.opsForValue().set(getCaptchaKey(request),captchaProducerText,CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);

        BufferedImage image = captchaProducer.createImage(captchaProducerText);
        //这里来用try-with-resources
        try (ServletOutputStream outputStream = response.getOutputStream()){
            ImageIO.write(image,"jpg",outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("获取流出错:{}",e.getMessage());
        }
    }

    private String getCaptchaKey(HttpServletRequest request){
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String key = "account-service:captcha:"+CommonUtil.MD5(ip+userAgent);
        log.info("验证码key:{}",key);
        return key;
    }


    /**
     * 测试发送验证码的接口，主要用于对比优化前后区别
     * @return
     */
    @RequestMapping("send_code")
    public JsonData sendCode(@RequestBody SendCodeRequest sendCodeRequest,HttpServletRequest request){
        String key = getCaptchaKey(request);
        String cacheCaptcha = redisTemplate.opsForValue().get(key);
        String captcha = sendCodeRequest.getCaptcha();
        if(cacheCaptcha!=null && cacheCaptcha !=null && cacheCaptcha.equalsIgnoreCase(captcha)){
            //成功的话可以把缓存中的captcha删除
            redisTemplate.delete(key);
            //因为后续的notify可能不仅仅用于用户注册，所以这个接口传入一个枚举类
            JsonData jsonData = notifyService.sendCode(SendCodeEnum.USER_REGISTER,sendCodeRequest.getTo());
            return jsonData;
        }else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
    }

}
