package ooad.demo.Service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    // 告诉spring这是异步
    @Async
    public void hello(){
        try{
            Thread.sleep(3000);
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("数据正在处理中");
    }
}
