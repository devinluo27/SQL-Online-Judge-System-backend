package ooad.demo.judge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckDocker {
    /**
     * Check every 15 seconds
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void checkIfStillRunning(){
        ConcurrentHashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
        Iterator<String> iterator = map.keySet().iterator();

        while(iterator.hasNext()) {
            String key = iterator.next();
            if (!key.contains("_trigger")) {
                continue;
            }
            DockerPool dockPool = map.get(key);
            synchronized (dockPool.getExecutingList()) {
                // beware of concurrent modification exception
                Iterator<Docker> docker_itr = dockPool.getExecutingList().iterator();
                while (docker_itr.hasNext()) {
                    Docker docker = docker_itr.next();
                    if (System.currentTimeMillis() - docker.getExec_start().getTime() > 1e4){
                        log.info("force remove Docker: " + docker.getDocker_name());
                        try {
                            dockPool.RemoveDockerOnly(docker.getDocker_name());
                        } catch (IOException | JSchException e) {
                            e.printStackTrace();
                        }
                        // remove it from executingList
                        docker_itr.remove();
                    }
                }
            }
        }

    }
}
