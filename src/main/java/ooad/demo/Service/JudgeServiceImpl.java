package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//@PropertySource(value = "classpath:application.yml")
@Service
//@ConfigurationProperties(prefix = "config")
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    RecordMapper recordMapper;

    private static final Random random = new Random();

    @Value("${judge.dockerPool.docker.num}")
    private static int dockerNum = 4;

    public static int getDockerNum() {
        return dockerNum;
    }

    public int a(){
        System.out.println("num" + dockerNum);
        return dockerNum;
    }

    @Async
    public void judgeCodeDocker(int record_id, String question_id,
                                String standard_ans, String code,
                                boolean isOrder, int operation_type, String sql_type) throws IOException, JSchException {
        if (ManageDockersPool.getInstance().getDockersPoolHashMap().get(question_id) == null){
//            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
            // who gets the lock first can create a dockerPool as follows
            synchronized (ManageDockersPool.getInstance().getCreateDockLock()){
                ManageDockersPool manageDockersPool = ManageDockersPool.getInstance();
                if (manageDockersPool.getDockersPoolHashMap().get(question_id) == null ){
                    HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
                    int randomDockerID = random.nextInt(100000000);
                    while (manageDockersPool.getDockerIDList().contains(randomDockerID)){
                        randomDockerID = random.nextInt(100000000);
                    }
                    map.put(question_id,
                            new DockerPool(dockerNum, randomDockerID, 0,"film",
                                    "/data2/DBOJ/DockerTest/film.sql"));
                    manageDockersPool.getDockerIDList().add(randomDockerID);
                }
            }
        }
        HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
        ArrayList<String> dockers = (map.get(question_id)).getRunningList();
        int rand = random.nextInt(dockers.size());
        String dockID = dockers.get(rand);

//        System.out.println(dockID);

        Judge.QUERY_RESULT response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);

        int score = response.getScore();
        int status = 2;
        switch (score){
            case 100: status =1; break; // accept
            case 0:   status = -1; break; // wrong
            case -1:  status = -2;break; // exception
        }
        double running_time = response.getExec_time();
        recordMapper.setRecordStatus(record_id, status, running_time);
    }
}
