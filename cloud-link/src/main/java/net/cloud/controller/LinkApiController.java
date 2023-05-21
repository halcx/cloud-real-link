package net.cloud.controller;

import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.ShortLinkStateEnum;
import net.cloud.service.LogService;
import net.cloud.service.ShortLinkService;
import net.cloud.utils.CommonUtil;
import net.cloud.vo.ShortLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;

//因为不需要返回json数据，所以用这个注解
@Controller
@Slf4j
public class LinkApiController {

    @Autowired
    private ShortLinkService shortLinkService;

    @Autowired
    private LogService logService;

    /**
     * 做跳转的时候我们用302还是301？
     * 1、301是永久重定向，302是临时重定向
     * 2、短地址一经生成就不会再变化，所以用301会减轻服务器的压力
     * 3、但是如果使用了301，就无法统计到短地址被点击的次数
     * 4、所以选择了302，虽然会导致服务器的压力一定水平的提高，但是我们会有更多数据可供分析
     * @param shortLinkCode
     * @param request
     * @param response
     */
    @GetMapping(path = "/{shortLinkCode}")
    public void dispatch(@PathVariable(name = "shortLinkCode")String shortLinkCode,
                         HttpServletRequest request, HttpServletResponse response){
        logService.recordShortLinkLog(shortLinkCode);
        try {
            log.info("短链码:{}",shortLinkCode);
            //判断短链码是否合规
            if(isLetterDigit(shortLinkCode)){
                //查找短链
                ShortLinkVO shortLinkVO = shortLinkService.parseShortLinkCode(shortLinkCode);
                if(isVisitable(shortLinkVO)){

                    String originalUrl = CommonUtil.removeUrlPrefix(shortLinkVO.getOriginalUrl());

                    response.setHeader("Location",originalUrl);
                    //302跳转
                    response.setStatus(HttpStatus.FOUND.value());
                }else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return;
                }
            }
        }catch (Exception e){
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    /**
     * 判断是否过期和可用
     * @param shortLinkVO
     * @return
     */
    private static boolean isVisitable(ShortLinkVO shortLinkVO){
        //先判断时间有没有过期
        if ((shortLinkVO!=null && shortLinkVO.getExpired().getTime()> CommonUtil.getCurrentTimestamp())){
            //判断是否被锁定
            if(ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())){
                return true;
            }
        }else if(shortLinkVO!=null && shortLinkVO.getExpired().getTime() == -1){
            if(ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())){
                return true;
            }
        }
        return false;
    }

    /**
     *  正则表达式规则，仅包括数字和字母
     * @param str
     * @return
     */
    private static boolean isLetterDigit(String str){
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }
}
