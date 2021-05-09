package ooad.demo;

import cn.shuibo.annotation.EnableSecurity;
import com.jcraft.jsch.JSchException;
import ooad.demo.judge.Docker;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.ManageDockersPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EnableAsync
@EnableSecurity
@EnableScheduling
@SpringBootApplication
public class DemoApplication {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public final static Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        logger.debug("Start Springboot!");
    }

    @PreDestroy
    public void destroy() {
        try {
            ConcurrentHashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
            Iterator<String> iterator = map.keySet().iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                DockerPool dockPool = map.get(key);
                synchronized (dockPool.getRunningList()) {
                    dockPool.getRunningList().addAll(dockPool.getExecutingList());
                    dockPool.getRunningList().addAll(dockPool.getSleepingList());
                    for (Docker docker: dockPool.getRunningList()) {
                        try {
                            dockPool.RemoveDockerOnly(docker.getDocker_name());
                            System.out.println("Removing" + docker.getDocker_name());
                        } catch (IOException | JSchException e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
            cleanRedis();
            System.out.println("Finish Removing Dockers");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private  void cleanRedis() {
        Set<String> keys = stringRedisTemplate.keys("*");
        assert keys != null;
        Iterator<String> it1 = keys.iterator();
        while (it1.hasNext()) {
            stringRedisTemplate.delete(it1.next());
        }
    }

}
