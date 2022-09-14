package net.cloud.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import net.cloud.service.NotifyService;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/account/v1/")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private Producer captchaProducer;

    /**
     * 生成验证码
     * @param request
     * @param response
     */
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        String captchaProducerText = captchaProducer.createText();
        log.info("验证码内容:{}",captchaProducerText);

        //存储到redis里面，配置过期时间 todo
        BufferedImage image = captchaProducer.createImage(captchaProducerText);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"jpg",outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("获取流出错:{}",e.getMessage());
        }

    }


    /**
     * 测试发送验证码的接口，主要用于对比优化前后区别
     * @return
     */
    @RequestMapping("send_code")
    public JsonData sendCode(){

        return JsonData.buildSuccess();
    }

}
