package net.cloud.annotation;

import java.lang.annotation.*;

/**
 * 自定义防止重复提交
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {
    /**
     * 定义方式类型
     * param: 方法参数形式
     * token：令牌形式
     */
    enum Type{PARAM,TOKEN}

    /**
     * 默认用方法参数来做防止重复提交
     * @return
     */
    Type limitType() default Type.PARAM;

    /**
     * 加锁过期时间，默认5s
     * @return
     */
    long lockTime() default 5;

}
