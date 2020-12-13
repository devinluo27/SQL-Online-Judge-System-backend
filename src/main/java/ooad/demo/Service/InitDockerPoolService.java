package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.ManageDockersPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

@Service
public class InitDockerPoolService {

    private static final Random random = new Random();

    @Value("${judge.dockerPool.docker.num}")
    private int dockerNum;

    private final String query = "_query";

    private final String trigger = "_trigger";

    /***
     *
     * @param database_id
     * @param operation_type 1: select 2: trigger
     * @throws IOException
     * @throws JSchException
     */
    public String InitDockerPool(Integer database_id, Integer operation_type) throws IOException, JSchException {
        String mapKey;
        if(operation_type == 1){
            mapKey = database_id + query;
        }
        else {
            mapKey = database_id + trigger;
        }
        if (ManageDockersPool.getInstance().getDockersPoolHashMap().get(mapKey) == null){
//            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
            // who gets the lock first can create a dockerPool as follows
            synchronized (ManageDockersPool.getInstance().getCreateDockerPoolLock()){
                ManageDockersPool manageDockersPool = ManageDockersPool.getInstance();
                if (manageDockersPool.getDockersPoolHashMap().get(mapKey) == null){
                    System.out.println("Init dockersPool " + mapKey);
                    HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
                    int randomDockerID = random.nextInt(100000000);
                    while (manageDockersPool.getDockerPoolIDList().contains(randomDockerID)){
                        randomDockerID = random.nextInt(100000000);
                    }
                    map.put(mapKey,
                            new DockerPool(dockerNum, randomDockerID, 0,"film",
                                    "/data2/DBOJ/DockerTest/film.sql"));
                    manageDockersPool.getDockerPoolIDList().add(randomDockerID);
                }
            }
        }
        return mapKey;
    }
}
