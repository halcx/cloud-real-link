package net.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolTaskConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //线程池创建的核⼼线程数，线程池维护线程的最少数，即使没有任务需要执⾏，也会⼀直存活
        //如果设置allowCoreThreadTimeout=true（默认false）时，核⼼线程会超时关闭
        threadPoolTaskExecutor.setCorePoolSize(32);
        //最⼤线程池数，当线程数>=corePoolSize，且任务队列已满时。线程池会创建新线程来处理任务
        //当线程数=maxPoolSize，且任务队列已满时，线程池会拒绝处理任务⽽抛出异常
        threadPoolTaskExecutor.setMaxPoolSize(64);
        //缓存队列（阻塞队列）当核⼼线程数达到最⼤时，新任务会放在队列中排队等待执⾏
        //阻塞队列在内存中，如果过长，或者宕机，则有可能会发生oom
        //采用异步+阻塞队列存缓存之后，开始的QPS很高，后面降低，是因为阻塞队列满了，PoolSize也满了，进程就会被拒绝，则QPS下降
        threadPoolTaskExecutor.setQueueCapacity(10000);
        //当线程空闲时间达到keepAliveTime时，线程会退出，直到线程数=corePoolSize
        //允许线程空闲时间60秒，当maxPoolSize的线程在空闲时间到达的时候销毁
        //如果allowCoreThreadTimeout=true，则会直到线程数=0
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        //spring 提供的 ThreadPoolTaskExecutor 线程池，是有setThreadNamePrefix() ⽅法的。
        //jdk 提供的ThreadPoolExecutor 线程池是没有setThreadNamePrefix() ⽅法的
        threadPoolTaskExecutor.setThreadNamePrefix("自定义线程前缀-");
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy()：交由调⽤⽅线程运⾏，⽐如main 线程；如果添加到线程池失败，那么主线程会⾃⼰去执⾏该任务，不会等待线程池中的线程去执⾏
        //AbortPolicy()：该策略是线程池的默认策略，如果线程池队列满了丢掉这个任务并且抛出RejectedExecutionException异常。
        //DiscardPolicy()：如果线程池队列满了，会直接丢掉这个任务并且不会有任何异常
        //DiscardOldestPolicy()：丢弃队列中最⽼的任务，队列满了，会将最早进⼊队列的任务删掉腾出空间，再尝试加⼊队列
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}

