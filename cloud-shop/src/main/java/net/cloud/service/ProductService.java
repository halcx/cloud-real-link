package net.cloud.service;

import net.cloud.vo.ProductVO;

import java.util.List;

public interface ProductService {
    /**
     * 获得所有商品详情
     * @return
     */
    List<ProductVO> list();

    ProductVO findDetailById(long productId);
}
