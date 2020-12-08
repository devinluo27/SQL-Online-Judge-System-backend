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
        if( poolSize < 0.5 * dockerPool.getPoolSize()){
            // DockerPool中没有docker了 重新建
            synchronized (dockerPool.getFillDockerPoolLock()){
                if(dockerPool.getRunningList().size() < 0.5 * dockerPool.getPoolSize()){
                    dockerPool.rebuildDocker((int)(0.5 * dockerPool.getPoolSize() - 1));
                }
            }
        }
    }
}
