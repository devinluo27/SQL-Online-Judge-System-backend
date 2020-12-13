package ooad.demo;

import cn.shuibo.annotation.EnableSecurity;
import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.ManageDockersPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@EnableAsync
@EnableSecurity
@SpringBootApplication
public class DemoApplication {

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
        System.out.println("Finish Removing Dockers");
    }

}
