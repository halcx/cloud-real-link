package net.cloud.controller;


import net.cloud.service.DomainService;
import net.cloud.utils.JsonData;
import net.cloud.vo.DomainVO;
import org.nustaq.offheap.structs.Align;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Wxh
 * @since 2022-09-27
 */
@RestController
@RequestMapping("/api/domain/v1")
public class DomainController {

    @Autowired
    private DomainService domainService;

    @GetMapping("list")
    public JsonData listAll(){
        List<DomainVO> list = domainService.listAll();
        return JsonData.buildSuccess(list);
    }

}

