package net.cloud.manager;

import net.cloud.model.ProductDO;

import java.util.List;

public interface ProductManager {
    List<ProductDO> list();

    ProductDO findDetailById(long productId);
}
