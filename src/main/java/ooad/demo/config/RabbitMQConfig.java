//package ooad.demo.config;
//
//import org.springframework.amqp.core.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//    static final String QUEUE = "direct_queue";
//    /**
//     * Direct模式
//     * @return
//     */
//    @Bean
//    public Queue directQueue() {
//        // 第一个参数是队列名字， 第二个参数是指是否持久化
//        return new Queue(QUEUE, true);
//    }
//}