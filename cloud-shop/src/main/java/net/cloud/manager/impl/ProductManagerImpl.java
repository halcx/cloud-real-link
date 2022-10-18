package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.ProductManager;
import net.cloud.mapper.ProductMapper;
import net.cloud.model.ProductDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ProductManagerImpl implements ProductManager {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductDO> list() {
        return productMapper.selectList(null);
    }

    @Override
    public ProductDO findDetailById(long productId) {
        return productMapper.selectOne(new QueryWrapper<ProductDO>().eq("id",productId));
    }
}
