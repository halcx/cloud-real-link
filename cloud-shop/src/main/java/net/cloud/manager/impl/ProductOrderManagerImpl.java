package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.ProductManager;
import net.cloud.manager.ProductOrderManager;
import net.cloud.mapper.ProductMapper;
import net.cloud.mapper.ProductOrderMapper;
import net.cloud.model.ProductDO;
import net.cloud.model.ProductOrderDO;
import net.cloud.vo.ProductOrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductOrderManagerImpl implements ProductOrderManager {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Override
    public int add(ProductOrderDO productOrderDO) {
        return productOrderMapper.insert(productOrderDO);
    }

    @Override
    public ProductOrderDO findByOutTradeNoAndAccountNo(String outTradeNo, Long accountNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo)
                .eq("account_no", accountNo));
        return productOrderDO;
    }

    @Override
    public int updateOrderPayState(String outTradeNo, Long accountNo, String newState, String oldState) {
        int rows = productOrderMapper.update(null,new UpdateWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo)
                .eq("account_no", accountNo)
                .eq("state",oldState)
                .set("state",newState));
        return rows;
    }

    @Override
    public Map<String, Object> page(int page, int size, Long accountNo, String state) {
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        IPage<ProductOrderDO> productOrderDOIPage;
        if(StringUtils.isBlank(state)){
            //如果state为空的话就查询全部state
            productOrderDOIPage = productOrderMapper.selectPage(pageInfo,new QueryWrapper<ProductOrderDO>()
                    .eq("account_no", accountNo));
        }else {
            productOrderDOIPage = productOrderMapper.selectPage(pageInfo,new QueryWrapper<ProductOrderDO>()
                    .eq("account_no", accountNo)
                    .eq("state",state));
        }
        List<ProductOrderDO> records = productOrderDOIPage.getRecords();
        List<ProductOrderVO> productOrderVOS = records.stream().map(obj -> {
            ProductOrderVO productOrderVO = new ProductOrderVO();
            BeanUtils.copyProperties(obj, productOrderVO);
            return productOrderVO;
        }).collect(Collectors.toList());

        Map<String,Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record",productOrderDOIPage.getTotal());
        pageMap.put("total_page",productOrderDOIPage.getPages());
        pageMap.put("current_data",productOrderVOS);

        return pageMap;
    }

    @Override
    public int del(Long productOrderId, Long accountNo) {
        int update = productOrderMapper.update(null, new UpdateWrapper<ProductOrderDO>()
                .eq("id", productOrderId)
                .eq("account_no", accountNo)
                .set("del",1));
        return update;
    }
}
