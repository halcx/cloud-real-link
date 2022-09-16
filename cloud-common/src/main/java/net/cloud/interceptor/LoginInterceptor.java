package net.cloud.interceptor;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.BizCodeEnum;
import net.cloud.model.LoginUser;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JWTUtil;
import net.cloud.utils.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登陆拦截器
 * * 解密JWT
 * * 传递登录用户信息
 *   * attribute传递
 *   * threadLocal传递
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 线程是贯穿了整个周期的，都可以从threadLocal去拿到里面的变量，所以可以用于透传
     */
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //放行操作
        if(HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())){
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }

        String accessToken = request.getHeader("token");
        if(StringUtils.isBlank(accessToken)){
            //如果从头里面拿不到的话，再尝试从参数里面拿一下
            accessToken = request.getParameter("token");
        }

        if(StringUtils.isNotBlank(accessToken)){
            //解密token
            Claims claims = JWTUtil.checkJWT(accessToken);
            if(claims == null){
                //未登录 返回给前端
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            long accountNO = Long.parseLong(claims.get("account_no").toString());
            String headImg = (String) claims.get("head_img");
            String username = (String) claims.get("username");
            String mail = (String) claims.get("mail");
            String phone = (String) claims.get("phone");
            String auth = (String) claims.get("auth");
            LoginUser loginUser = LoginUser.builder().accountNo(accountNO)
                    .auth(auth)
                    .username(username)
                    .mail(mail)
                    .headImg(headImg)
                    .phone(phone)
                    .build();

            //透传信息给controller
            //方式一
            //request.setAttribute("loginUser",loginUser);
            //方式二 通过threadLocal
            threadLocal.set(loginUser);
            return true;
        }


        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //防止内存泄漏
        threadLocal.remove();
    }
}
