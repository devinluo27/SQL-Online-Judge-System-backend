package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.DataBaseMapper;
import ooad.demo.pojo.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DockerPoolService {
    private static final Random random = new Random();

    @Autowired
    DataBaseMapper dataBaseMapper;

    @Value("${judge.dockerPool.docker.num}")
    private int dockerNum;

    private final String query = "_query";

    private final String trigger = "_trigger";


    @Async
    public void createADocker(DockerPool dockerPool) throws IOException, JSchException {
        System.out.println("Refill DockerPool: * " + 1 + " * with dockers");
            int flag = 0;
            try {
                flag = dockerPool.rebuildDocker(1);
            } catch (JSchException jSchException){
                createADocker(dockerPool);
            }
    }

    /***
     *
     * @param database_id
     * @param operation_type 1: select 2: trigger
     * @throws IOException
     * @throws JSchException
     */
    public String InitDockerPool(Integer database_id, String operation_type) throws IOException, JSchException {
        String mapKey;
        if(operation_type.equals("query")){
            mapKey = database_id + query;
        }
        else {
            mapKey = database_id + trigger;
        }
        // TODO: OPERATION_TYPE CHECK
        if (ManageDockersPool.getInstance().getDockersPoolHashMap().get(mapKey) == null){
//            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
            // who gets the lock first can create a dockerPool as follows
            synchronized (ManageDockersPool.getInstance().getCreateDockerPoolLock()){
                ManageDockersPool manageDockersPool = ManageDockersPool.getInstance();
                if (manageDockersPool.getDockersPoolHashMap().get(mapKey) == null){
                    log.info("Init dockersPool: " + mapKey);
                    ConcurrentHashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
                    int randomDockerID = random.nextInt(100000000);
                    while (manageDockersPool.getDockerPoolIDList().contains(randomDockerID)){
                        randomDockerID = random.nextInt(100000000);
                    }
                    Database database = dataBaseMapper.selectDatabaseById(database_id);
                    if (database == null){
                        throw new NullPointerException("##Database Doesn't exist!##");
                    }
                    String database_name = database.getDatabase_remote_name();
                    // TODO: 文件后缀更新
                    String database_full_address = database.getDatabase_remote_path() + database_name;
                    map.put(mapKey,
//                            new DockerPool(dockerNum, randomDockerID, 0,"film",
//                                    "/data2/DBOJ/DockerTest/film.sql")

                            // TODO： 大锅 默认为postgresql判题

                            new DockerPool(dockerNum, randomDockerID, 0, database_name,
                                    database_full_address)
                    );
                    manageDockersPool.getDockerPoolIDList().add(randomDockerID);
                    log.info("Init dockersPool Finished: " + mapKey);
                }
            }
        }
        return mapKey;
    }

    /***
     * judge whether a docker pool has been init
     * @param database_id
     * @param operation_type
     * @return
     */
    public boolean isInitDockerPool(int database_id, String operation_type){
        String mapKey;
        if(operation_type.equals("query")){
            mapKey = database_id + query;
        }
        else {
            mapKey = database_id + trigger;
        }
        return ManageDockersPool.getInstance().getDockersPoolHashMap().get(mapKey) != null;
    }



}
