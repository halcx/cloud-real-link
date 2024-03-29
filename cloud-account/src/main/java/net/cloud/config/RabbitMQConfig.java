package net.cloud.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@Data
public class RabbitMQConfig {
    /**
     * 消息转换器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    //================流包处理：⽤户初始化福利==================================
    /**
     * 交换机
     */
    private String trafficEventExchange = "traffic.event.exchange";
    /**
     * ⽤户注册 免费流￾包新增 队列
     */
    private String trafficFreeInitQueue = "traffic.free_init.queue";
    /**
     * ⽤户注册 免费流￾包新增 队列路由key
     *
     */
    private String trafficFreeInitRoutingKey = "traffic.free_init.routing.key";
    /**
     * 创建交换机 Topic类型
     * ⼀般⼀个微服务⼀个交换机
     * @return
     */
    @Bean
    public Exchange trafficEventExchange(){
        return new TopicExchange(trafficEventExchange,true,false);
    }
    /**
     * 队列的绑定关系建⽴:新⽤户注册免费流￾包
     * @return
     */
    @Bean
    public Binding trafficFreeInitBinding(){
        return new Binding(trafficFreeInitQueue,Binding.DestinationType.QUEUE, trafficEventExchange,trafficFreeInitRoutingKey,null);
    }
    /**
     * 免费流包队列
     */
    @Bean
    public Queue trafficFreeInitQueue(){
        return new Queue(trafficFreeInitQueue,true,false,false);
    }


    //================流量包扣减，创建短链死信队列配置==================================
    // 发送锁定流￾包消息-》延迟exchange-》lock.queue-》死信exchange-》release.queue 延迟队列，不能被监听消费
    /**
     * 第⼀个队列延迟队列，
     */
    private String trafficReleaseDelayQueue = "traffic.release.delay.queue";
    /**
     * 第⼀个队列的路由key
     * 进⼊队列的路由key
     */
    private String trafficReleaseDelayRoutingKey = "traffic.release.delay.routing.key";
    /**
     * 第⼆个队列，被监听恢复流包的队列
     */
    private String trafficReleaseQueue = "traffic.release.queue";
    /**
     * 第⼆个队列的路由key
     *
     * 即进⼊死信队列的路由key
     */
    private String trafficReleaseRoutingKey="traffic.release.routing.key";
    /**
     * 过期时间，毫秒,1分钟
     */
    private Integer ttl = 60000;

    /**
     * 延迟队列
     */
    @Bean
    public Queue trafficReleaseDelayQueue(){
        Map<String,Object> args = new HashMap<>(3);
        args.put("x-message-ttl",ttl);
        args.put("x-dead-letter-exchange", trafficEventExchange);
        args.put("x-dead-letter-routing-key",trafficReleaseRoutingKey);
        return new Queue(trafficReleaseDelayQueue,true,false,false,args);
    }
    /**
     * 死信队列，普通队列，⽤于被监听
     */
    @Bean
    public Queue trafficReleaseQueue(){
        return new Queue(trafficReleaseQueue,true,false,false);
    }
    /**
     * 第⼀个队列，即延迟队列的绑定关系建⽴
     * @return
     */
    @Bean
    public Binding trafficReleaseDelayBinding(){
        return new Binding(trafficReleaseDelayQueue,Binding.DestinationType.QUEUE,
                trafficEventExchange,trafficReleaseDelayRoutingKey,null);
    }
    /**
     * 死信队列绑定关系建⽴
     * @return
     */
    @Bean
    public Binding trafficReleaseBinding(){
        return new Binding(trafficReleaseQueue,Binding.DestinationType.QUEUE,
                trafficEventExchange,trafficReleaseRoutingKey,null);
    }
}
