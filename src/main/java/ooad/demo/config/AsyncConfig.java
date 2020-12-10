package ooad.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

//    核心线程池未满时: 接收任务，创建线程并执行该任务。
//    核心线程池已满时: 接收任务，任务进入等待队列等待。
//    核心线程池满且等待队列也满时: 接收任务，并创建线程执行该任务。
//    核心线程满，等待队列满且最大线程池也满时: 接收任务，按丢弃策略处理该任务。

    @Value("${thread.corePoolSize}")
    private int corePoolSize = 4;

    @Value("${thread.maxPoolSize}")
    private int maxPoolSize = 30;

    @Value("${thread.queueCapacity}")
    private int queueCapacity = 4;

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("LocustTask-");
        executor.initialize();
        return executor;
    }

}
