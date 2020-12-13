package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    DockerPoolService dockerPoolService;


    private final String query = "_query";

    private final String trigger = "_trigger";


    @Async
    public void judgeCodeDocker(int record_id, Integer question_id,
                                String code,
                                boolean isOrder, String sql_type) throws IOException, JSchException {

        Question q = questionMapper.getInfoForJudge(question_id);
        String standard_ans = q.getQuestion_standard_ans();
        String database_id = String.valueOf(q.getDatabase_id());
        Integer operation_type = q.getOperation_type();
        String mapKey;
        if(operation_type == 1){
            mapKey = database_id + query;
        }
        else {
            mapKey = database_id + trigger;
        }

        HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
        DockerPool usedDockerPool = map.get(mapKey);
        ArrayList<String> dockers = usedDockerPool.getRunningList();

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
                usedDockerPool.getRunningList().remove(dockID);
                usedDockerPool.getSleepingList().remove(dockID);
            }
            //  TODO: 硬编码 postgres： 0
            response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);
            System.out.print("docker in service: ");

            System.out.println("remove_docker_id" + dockID);
            usedDockerPool.RemoveDockerOnly(dockID);
            System.out.println();
        }
        else if(operation_type == 2){
            // trigger
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
                usedDockerPool.getRunningList().remove(dockID);
                usedDockerPool.getSleepingList().remove(dockID);
            }
            //  TODO: 更换各种硬编码 postgres： 0
            System.out.println("DockerId: " + dockID);

            response =  Judge.EXEC_TRIGGER("/data2/DBOJ/week14Sigiin/week14_sign_in_ans.sql",
                    code,
                    "/data2/DBOJ/week14Sigiin/week14_sign_in_test.sql",
                    10,
                    dockID,
                    0,
                    "cars"
                    );

//            response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);

            System.out.println("remove_docker_id" + dockID);
            usedDockerPool.RemoveDockerOnly(dockID);
            System.out.println();
        }
        else {
            // error
            response = new Judge.QUERY_RESULT(-2, -1, "", "");
        }

        int score = response.getScore();
        int status = 0;
        switch (score){
            case 100: status = 1; break; // accept
            case 0:   status = -1; break; // wrong
            case -1:  status = -2;break; // exception
            case -2: status = -3; break; // 后端判题出现异常 请稍后再试
        }
        double running_time = response.getExec_time();
        recordMapper.setRecordStatus(record_id, status, running_time);
        System.out.println("current_size_after_judge: " + usedDockerPool.getRunningList().size());
        System.out.println(System.currentTimeMillis());
    }

}
