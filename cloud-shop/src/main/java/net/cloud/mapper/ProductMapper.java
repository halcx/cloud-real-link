package net.cloud.mapper;

import net.cloud.model.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Wxh
 * @since 2022-10-15
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

}
