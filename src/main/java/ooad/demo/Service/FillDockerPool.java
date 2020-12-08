package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FillDockerPool {
    @Async
    public void checkAndFillDockerPool(DockerPool dockerPool, int poolSize) throws IOException, JSchException {
        if( poolSize < dockerPool.getPoolSize() / 2){
            // DockerPool中没有docker了 重新建
            synchronized (dockerPool.getFillDockerPoolLock()){
                if(dockerPool.getRunningList().size() < dockerPool.getPoolSize() / 2){
                    // create 4 dockers
                    int numRefill = dockerPool.getPoolSize() / 2;
                    System.out.println("Refill DockerPool: * " + numRefill + " *with dockers");
                    dockerPool.rebuildDocker(numRefill);
                }
            }
        }
    }
}
