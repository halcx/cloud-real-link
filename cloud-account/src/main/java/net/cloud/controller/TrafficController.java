package net.cloud.controller;

import net.cloud.controller.request.TrafficPageRequest;
import net.cloud.controller.request.UseTrafficRequest;
import net.cloud.service.TrafficService;
import net.cloud.utils.JsonData;
import net.cloud.vo.TrafficVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/api/traffic/v1")
@RestController
public class TrafficController {

    @Autowired
    private TrafficService trafficService;

    @Value("${rpc.token}")
    private String rpcToken;

    @RequestMapping("page")
    public JsonData pageAvailable(@RequestBody TrafficPageRequest request){
        Map<String,Object> map = trafficService.pageAvailable(request);
        return JsonData.buildSuccess(map);
    }

    /**
     * 查找某个流量包详情
     * @param trafficId
     * @return
     */
    @GetMapping("detail/{trafficId}")
    public JsonData detail(@PathVariable("trafficId") Long trafficId){
        TrafficVO trafficVO = trafficService.detail(trafficId);
        return JsonData.buildSuccess(trafficVO);
    }

    /**
     * 使用流量包 TODO
     * @param useTrafficRequest
     * @param request
     * @return
     */
    @PostMapping("reduce")
    public JsonData useTraffic(@RequestBody UseTrafficRequest useTrafficRequest, HttpServletRequest request){
        String requestToken = request.getHeader("rpc-token");
        if(rpcToken.equalsIgnoreCase(requestToken)){
            JsonData jsonData = trafficService.reduce(useTrafficRequest);
            return jsonData;
        }else {
            return JsonData.buildError("非法访问");
        }
    }
}
