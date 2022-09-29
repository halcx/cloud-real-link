package net.cloud.controller;


import net.cloud.controller.request.ShortLinkAddRequest;
import net.cloud.service.ShortLinkService;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("add")
    public JsonData createShortLink(@RequestBody ShortLinkAddRequest request){
        JsonData jsonData = shortLinkService.createShortLink(request);
        return jsonData;
    }
}

