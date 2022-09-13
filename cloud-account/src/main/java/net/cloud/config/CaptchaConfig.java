package net.cloud.config;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    /**
     * 验证码配置
     * Kaptcha配置类名
     * @return
     */
    @Bean
    @Qualifier("captchaProducer")
    public DefaultKaptcha kaptcha() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        //    properties.setProperty(Constants.KAPTCHA_BORDER, "yes");
        //    properties.setProperty(Constants.KAPTCHA_BORDER_COLOR, "220,220,220");
        //    //properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "38,29,12");
        //    properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "147");
        //    properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "34");
        //    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "25");
        //    //properties.setProperty(Constants.KAPTCHA_SESSION_KEY, "code");
        //验证码个数
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        //    properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Courier");
        //字体间隔
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE,"8");
        //干扰线颜色
        //    properties.setProperty(Constants.KAPTCHA_NOISE_COLOR, "white");
        //干扰实现类
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        //图片样式
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.WaterRipple");
        //文字来源
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
