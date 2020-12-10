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

    private final String query = "query";
    private final String trigger = "trigger";

    /***
     *
     * @param database_id_int
     * @param operation_type 1: select 2: trigger
     * @throws IOException
     * @throws JSchException
     */
    public void  InitDockerPool(Integer database_id_int, Integer operation_type) throws IOException, JSchException {
        String database_id = String.valueOf(database_id_int);
        if (ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id) == null){
//            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
            // who gets the lock first can create a dockerPool as follows
            synchronized (ManageDockersPool.getInstance().getCreateDockerPoolLock()){
                ManageDockersPool manageDockersPool = ManageDockersPool.getInstance();
                if (manageDockersPool.getDockersPoolHashMap().get(database_id) == null ){
                    System.out.println("Init dockersPool " + database_id);
                    HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
                    int randomDockerID = random.nextInt(100000000);
                    while (manageDockersPool.getDockerIDList().contains(randomDockerID)){
                        randomDockerID = random.nextInt(100000000);
                    }
                    map.put(database_id,
                            new DockerPool(dockerNum, randomDockerID, 0,"film",
                                    "/data2/DBOJ/DockerTest/film.sql"));
                    manageDockersPool.getDockerIDList().add(randomDockerID);
                }
            }
        }
    }
}
