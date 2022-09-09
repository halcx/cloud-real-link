package net.cloud.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Wxh
 * @since 2022-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("traffic_task")
public class TrafficTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long accountNo;

    private Long trafficId;

    private Integer useTimes;

    /**
     * 锁定状态锁定LOCK  完成FINISH-取消CANCEL
     */
    private String lockState;

    /**
     * 唯一标识
     */
    private String messageId;

    private Date gmtCreate;

    private Date gmtModified;


}
