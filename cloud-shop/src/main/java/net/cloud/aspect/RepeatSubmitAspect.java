package net.cloud.aspect;

import lombok.extern.slf4j.Slf4j;
import net.cloud.annotation.RepeatSubmit;
import net.cloud.constant.RedisKey;
import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.utils.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 切面类，防止重复提交
 */
@Aspect
@Component
@Slf4j
public class RepeatSubmitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 定义PointCut表达式：
     * - 方式一：@annotation：但执行的方法上拥有指定的注解时生效
     * - 方式二：execution:一般用于指定方法的执行
     */
    @Pointcut("@annotation(repeatSubmit)")
    public void pointCutNoRepeatSubmit(RepeatSubmit repeatSubmit){

    }
    /**
    * 环绕通知, 围绕着⽅法执⾏
            * @Around 可以⽤来在调⽤⼀个具体⽅法前和调⽤后来完成⼀些具体的任务。
            *
            * ⽅式⼀：单⽤ @Around("execution(*net.xdclass.controller.*.*(..))")可以
            * ⽅式⼆：⽤@Pointcut和@Around联合注解也可以（我们采⽤这个）
            *
            * 两种⽅式
            * ⽅式⼀：加锁 固定时间内不能重复提交
            * ⽅式⼆：先请求获取token，这边再删除token,删除成功则是第⼀次提交
     */
    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint,RepeatSubmit repeatSubmit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        //用于记录成功或者失败
        boolean res = false;

        /**
         * 防重提交类型
         */
        String type = repeatSubmit.limitType().name();

        if(type.equalsIgnoreCase(RepeatSubmit.Type.PARAM.name())){
            //方式一：参数形式防重复提交
            long lockTime = repeatSubmit.lockTime();

            String ipAddr = CommonUtil.getIpAddr(request);

            //拿到方法签名
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

            //拿到方法
            Method method = methodSignature.getMethod();

            //拿到类名
            String name = method.getDeclaringClass().getName();

            //构建key
            String key = String.format("%s-%s-%s-%s",ipAddr,name,method,accountNo);

            //加锁
            //res = redisTemplate.opsForValue().setIfAbsent(key,"1",lockTime, TimeUnit.SECONDS);
            // 分布式锁
            RLock lock = redissonClient.getLock(key);
            // 尝试加锁，最多等待2秒，上锁以后5秒自动解锁 [lockTime默认为5s, 可以自定义] 自旋
            res = lock.tryLock(2, lockTime, TimeUnit.SECONDS);
        }else {
            //方式二：令牌形式防重复提交
            String requestToken = request.getHeader("request-token");
            if(StringUtils.isBlank(requestToken)){
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
            }
            String key = String.format(RedisKey.SUBMIT_ORDER_TOKEN_KEY, accountNo, requestToken);
            /**
             * 提交表单的token key
             * 方式一：不用lua脚本获取再判断，之前是因为key组成是order:submit:accountNo,value是对应的token，所以要先获取再判断
             * 方式二：order:submit:accountNo:token，直接删除就完成了
             */
            res = redisTemplate.delete(key);
        }

        if (!res){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_REPEAT);
        }

        log.info("环绕通知执行前");
        Object obj = joinPoint.proceed();
        log.info("环绕通知执行后");
        return obj;
    }
}
