package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//@PropertySource(value = "classpath:application.yml")
@Service
//@ConfigurationProperties(prefix = "judge")
@Async
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    RecordMapper recordMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    FillDockerPool fillDockerPool;

    private static final Random random = new Random();

    @Value("${judge.dockerPool.docker.num}")
    private int dockerNum;


    public int a(){
        System.out.println("num" + dockerNum);
        return dockerNum;
    }

    @Async
    public void judgeCodeDocker(int record_id, Integer question_id,
                                String code,
                                boolean isOrder, String sql_type) throws IOException, JSchException {

        Question q = questionMapper.getInfoForJudge(question_id);
        String standard_ans = q.getQuestion_standard_ans();
        String database_id = String.valueOf(q.getDatabase_id());
        Integer operation_type = q.getOperation_type();

        if (ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id) == null){
//            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
            // who gets the lock first can create a dockerPool as follows
            synchronized (ManageDockersPool.getInstance().getCreateDockLock()){
                ManageDockersPool manageDockersPool = ManageDockersPool.getInstance();
                if (manageDockersPool.getDockersPoolHashMap().get(database_id) == null ){
                    System.out.println("Hello");
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

        HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
        ArrayList<String> dockers = (map.get(database_id)).getRunningList();
//        ArrayList<String> availableDockers = (map.get(database_id)).getAvailableList();

        DockerPool usedDockerPool = map.get(database_id);

        fillDockerPool.createADocker(usedDockerPool);
        System.out.println("current_size_before_judge: " + usedDockerPool.getRunningList().size());

        Judge.QUERY_RESULT response;
        if(operation_type == 1){
            // only query
//            int rand = random.nextInt(dockers.size());
//            String dockID = dockers.get(rand);
            String dockID;

            synchronized (usedDockerPool.getRunningList()){
                if(usedDockerPool.getRunningList().size() == 0){
                    // 等待某个docker 建好后唤醒它
                    // 放入等待队列
                    try {
                        System.out.println("Waiting! Rid: " + record_id);
                        usedDockerPool.getRunningList().wait();
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                dockID = dockers.get(0);
                map.get(database_id).getRunningList().remove(dockID);
                map.get(database_id).getSleepingList().remove(dockID);
            }

            response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);
            System.out.print("docker in service: ");

//            for (String name: usedDockerPool.getRunningList()){
//                System.out.print(name.substring(24) + " ");
//            }

            System.out.println("remove_docker_id" + dockID);
            (map.get(database_id)).RemoveDocker(dockID);
            System.out.println();

        }
        else if(operation_type == 2){
            // trigger
            String dockID;
            synchronized ((map.get(database_id)).getRunningList()){
                int rand = random.nextInt(dockers.size());
                dockID = dockers.get(rand);
                map.get(database_id).getRunningList().remove(dockID);
                map.get(database_id).getSleepingList().remove(dockID);
            }
            response =  new Judge.QUERY_RESULT(0, -1, "", "");
            (map.get(database_id)).RemoveDocker(dockID);
        }
        else {
            // error
            response = new Judge.QUERY_RESULT(0, -1, "", "");
        }

        int score = response.getScore();
        int status = 0;
        switch (score){
            case 100: status = 1; break; // accept
            case 0:   status = -1; break; // wrong
            case -1:  status = -2;break; // exception
        }
        double running_time = response.getExec_time();
        recordMapper.setRecordStatus(record_id, status, running_time);
        System.out.println("current_size_after_judge: " + usedDockerPool.getRunningList().size());
        System.out.println(System.currentTimeMillis());
    }

    // TODO: 死锁 当没有docker了 暂停全部判题 先建dockers 建一半的dockers
    public void reFillDockerPool(DockerPool dockerPool) throws IOException, JSchException {
        // DockerPool中没有docker了 重新建
        synchronized (dockerPool.getFillDockerPoolLockReach0()){
            dockerPool.setStatus(0);
            System.out.println("0 dockers and create!");
            if(dockerPool.getRunningList().size() < 0.5 * dockerPool.getPoolSize()){
                dockerPool.rebuildDocker((int)(0.5 * dockerPool.getPoolSize() - 1));
            }
            dockerPool.setStatus(1);
        }
    }

}
