package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.ProductManager;
import net.cloud.model.ProductDO;
import net.cloud.service.ProductService;
import net.cloud.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductManager productManager;

    @Override
    public List<ProductVO> list() {
        List<ProductDO> productDOList = productManager.list();
        List<ProductVO> collect = productDOList.stream().map(obj ->
            beanProcess(obj)
        ).collect(Collectors.toList());
        return collect;
    }

    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productManager.findDetailById(productId);
        return beanProcess(productDO);
    }

    private ProductVO beanProcess(ProductDO productDO){
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO,productVO);
        return productVO;
    }
}
