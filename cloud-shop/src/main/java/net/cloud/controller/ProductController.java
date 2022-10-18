package net.cloud.controller;

import net.cloud.service.ProductService;
import net.cloud.utils.JsonData;
import net.cloud.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product/v1")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 查看全部商品列表接口
     * @return
     */
    @GetMapping("list")
    public JsonData list(){
        List<ProductVO> productVOList = productService.list();
        return JsonData.buildSuccess(productVOList);
    }

    /**
     * 查看商品详情
     * @param productId
     * @return
     */
    @GetMapping("detail/{product_id}")
    public JsonData detail(@PathVariable("product_id") long productId){
        ProductVO productVO = productService.findDetailById(productId);
        return JsonData.buildSuccess(productVO);
    }
}
