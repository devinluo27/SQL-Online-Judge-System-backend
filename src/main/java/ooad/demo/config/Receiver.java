//package ooad.demo.config;
//
//
//import lombok.extern.slf4j.Slf4j;
//import ooad.demo.pojo.UserDB;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class Receiver {
//
//    // queues是指要监听的队列的名字
//    @RabbitListener(queues = RabbitMQConfig.QUEUE)
//    public void receiverDirectQueue(UserDB user) {
//        log.info("【receiverDirectQueue监听到消息】" + user.toString());
//    }
//
//}