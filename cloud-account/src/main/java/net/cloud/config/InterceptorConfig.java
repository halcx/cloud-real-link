package net.cloud.config;

import lombok.extern.slf4j.Slf4j;
import net.cloud.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 账号服务拦截器配置
 */
@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                //添加拦截的路径
                .addPathPatterns("/api/account/*/**","/api/traffic/*/**")
                //排除不拦截的
                .excludePathPatterns("/api/account/*/register","/api/account/*/upload","/api/account/*/login"
                        ,"/api/notify/*/captcha","/api/notify/*/send_code");

    }
}
