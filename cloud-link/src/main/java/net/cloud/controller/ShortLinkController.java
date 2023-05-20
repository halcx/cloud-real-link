package net.cloud.controller;


import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.controller.request.ShortLinkDelRequest;
import net.cloud.controller.request.ShortLinkPageRequest;
import net.cloud.controller.request.ShortLinkUpdateRequest;
import net.cloud.service.ShortLinkService;
import net.cloud.utils.JsonData;
import net.cloud.vo.ShortLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Wxh
 * @since 2022-09-21
 */
@RestController
@RequestMapping("/api/link/v1")
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;

    @Value("${rpc.token}")
    private String rpcToken;

    /**
     * 新增短链
     * @param request
     * @return
     */
    @PostMapping("add")
    public JsonData createShortLink(@RequestBody ShortLinkAddRequest request){
        JsonData jsonData = shortLinkService.createShortLink(request);
        return jsonData;
    }

    /**
     * 分页查询短链
     * @param request
     * @return
     */
    @RequestMapping("page")
    public JsonData pageByGroupId(@RequestBody ShortLinkPageRequest request){
        Map<String,Object>result = shortLinkService.pageByGroupId(request);
        return JsonData.buildSuccess(result);
    }

    /**
     * 删除短链
     * @param request
     * @return
     */
    @PostMapping("del")
    public JsonData del(@RequestBody ShortLinkDelRequest request){
        JsonData jsonData = shortLinkService.del(request);
        return jsonData;
    }

    /**
     * 更新短链
     * @param request
     * @return
     */
    @PostMapping("update")
    public JsonData update(@RequestBody ShortLinkUpdateRequest request){
        JsonData jsonData = shortLinkService.update(request);
        return jsonData;
    }

    @GetMapping( "check")
    JsonData check(@RequestParam("shortLinkCode") String shortLinkCode, HttpServletRequest request){
        String token = request.getHeader("rpc-token");
        if(rpcToken.equalsIgnoreCase(token)){
            ShortLinkVO shortLinkVO = shortLinkService.parseShortLinkCode(shortLinkCode);

            return shortLinkVO==null?JsonData.buildError("短链不存在"):JsonData.buildSuccess();
        }else {
            return JsonData.buildError("非法访问");
        }
    }
}

