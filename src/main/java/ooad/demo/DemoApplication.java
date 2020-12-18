package ooad.demo;

import cn.shuibo.annotation.EnableSecurity;
import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.ManageDockersPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

@EnableAsync
@EnableSecurity
@SpringBootApplication
public class DemoApplication {

    @Autowired
    private static StringRedisTemplate stringRedisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PreDestroy
    public static void destroy() throws IOException, JSchException {
        ArrayList<Integer> dockerPoolIDList = ManageDockersPool.getInstance().getDockerPoolIDList();
        HashMap<String, DockerPool> dockersPoolHashMap = ManageDockersPool.getInstance().getDockersPoolHashMap();
        for (Integer i : dockerPoolIDList){
            String[] dockerNames = dockersPoolHashMap.get(String.valueOf(i)).getRunningList().toArray(new String[0]);
            for (String name: dockerNames) {
                System.out.println("Removing" + name);
                dockersPoolHashMap.get(String.valueOf(i)).RemoveDocker(name);
            }
        }
        cleanRedis();
        System.out.println("Finish Removing Dockers");
    }

    private static  void cleanRedis() {
        Set<String> keys = stringRedisTemplate.keys("*");
        Iterator<String> it1 = keys.iterator();
        while (it1.hasNext()) {
            stringRedisTemplate.delete(it1.next());
        }
    }

}
