package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.judge.Docker;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.judge.Remote;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.QuestionTriggerMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.QuestionTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

//@PropertySource(value = "classpath:application.yml")
@Service
//@ConfigurationProperties(prefix = "judge")
@Slf4j
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    RecordMapper recordMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    QuestionTriggerMapper questionTriggerMapper;

    @Autowired
    DockerPoolService dockerPoolService;

    // TODO: !!!
//    @Autowired
//    DockerPool dockerPool;

    @Autowired
    Remote remote;

    @Autowired
    Judge judge;

    private Random random = new Random();

    private final String query = "_query";

    private final String trigger = "_trigger";

    static String checkIfRunningCMD = "docker ps --filter name=#DockerNAME# --filter status=running --format \"{{.Names}}\"";

    // TODO: THE SAME
    public boolean checkIfRunning(String DockerNAME) throws IOException, JSchException {
        String CMD = checkIfRunningCMD.replaceAll("#DockerNAME#",DockerNAME);
        Remote.Log log = remote.EXEC_CMD(new String[]{CMD}).get(0);
        return log.OUT != null && log.OUT.replaceAll("\n","").equals(DockerNAME);
    }


    @Async
    public void judgeCodeDocker(int record_id,
                                Question question, String code) throws IOException, JSchException {

        // get basic information of questions
        String standard_ans = question.getQuestion_standard_ans();
        String database_id = String.valueOf(question.getDatabase_id());
        String operation_type = question.getOperation_type();
        int question_id = question.getQuestion_id();
        String string_sql_type = question.getQuestion_sql_type();
        // specify sql type: 0 for postgresql
        int sql_type = getSqlTypeIndex(string_sql_type);
        boolean is_order = question.getIs_order();

        // key of hashmap 1. _query 2. _trigger
        String mapKey;
        if(operation_type.equals("query")){
            mapKey = database_id + query;
        }
        else {
            mapKey = database_id + trigger;
        }

        // get the only single instance from ManageDockersPool
        ConcurrentHashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersPoolHashMap();
        DockerPool usedDockerPool = map.get(mapKey);

        // get the arraylist of available dockers
        ArrayList<Docker> dockers = usedDockerPool.getRunningList();

        log.info("\n判题开始 " + "current_size_before_judge: " + usedDockerPool.getRunningList().size());

        Judge.QUERY_RESULT response = null;

        // TODO: QUERY Judging
        if(operation_type.equals("query")){
            Docker docker_selected;
            synchronized (usedDockerPool.getRunningList()){
                int rand = random.nextInt(dockers.size());
                if(usedDockerPool.getRunningList().size() == 0){
                    // 等待某个docker 建好后唤醒它
                    // 放入等待队列
                    try {
                        System.out.println("No Docker Available! Waiting! Record_id: " + record_id);
                        usedDockerPool.getRunningList().wait();
                    } catch(InterruptedException e) {
                        // TODO: 异常处理
                        e.printStackTrace();
                    }
                }
                docker_selected = dockers.get(rand);
            }
            String docker_name = docker_selected.docker_name;
            //  TODO: 硬编码 postgres： 0
            //  check if docker is Health 才判题
            if (checkIfRunning(docker_name)){
//                System.out.println("SQL Type: " + sql_type);
                response =  judge.EXEC_QUERY(standard_ans, code, docker_name, is_order, sql_type);
            }
            else {
                usedDockerPool.getRunningList().remove(docker_selected);
                usedDockerPool.getSleepingList().remove(docker_selected);
                usedDockerPool.RemoveDockerOnly(docker_name);
                usedDockerPool.rebuildDocker(1);
            }
            System.out.println("Query_Docker_ID: " + docker_name);
        }

        // TODO: TRIGGER
        else if(operation_type.equals("trigger")){
//            String dockID = null;
            Docker docker_selected = null;
            // TODO: !!!!!!! 动态判题
            try{
                Map<String, String> triggerJudgeInfo = questionTriggerMapper.getTriggerQuestionJudgeInfoByQid(question_id);
                String ans_table_file_full_path = triggerJudgeInfo.get("ans_table_file_full_path");
                String test_data_file_full_path = triggerJudgeInfo.get("test_data_file_full_path");
                String target_table = triggerJudgeInfo.get("target_table");
                String test_config = triggerJudgeInfo.get("test_config");

                synchronized (usedDockerPool.getRunningList()) {
                    if(usedDockerPool.getRunningList().size() == 0){
                        // 等待某个docker 建好后唤醒它
                        // 放入等待队列
                        try {
                            log.warn("Trigger Waiting! Rid: " + record_id);
                            usedDockerPool.getRunningList().wait();
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // TODO:
                    docker_selected = dockers.get(0);
                    usedDockerPool.getRunningList().remove(docker_selected);
                    usedDockerPool.getSleepingList().remove(docker_selected);
                }

                docker_selected.set_running(true);
                docker_selected.setExec_start(new Timestamp(System.currentTimeMillis()));

                synchronized (usedDockerPool.getExecutingList()) {
                    usedDockerPool.getExecutingList().add(docker_selected);
                }


                ans_table_file_full_path = "/data/testData/answer.sql";
                test_data_file_full_path = "/data/testData/testData.sql";
                String preJudgePath = "/data/testData/preJudge.sql";
                String[] targetTable = {"account","account_log"};
                String[] colConfig = {"*", "user_id, password"};
                // TODO: 更换各种硬编码 postgres： 0 ================
                response = judge.EXEC_TRIGGER(
                        ans_table_file_full_path,
                        code,
                        test_data_file_full_path,
                        docker_selected.docker_name,
                        targetTable,
                        preJudgePath,
                        colConfig
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                log.info("remove_docker_id: " + docker_selected.getDocker_name());
                usedDockerPool.RemoveDockerOnly(docker_selected.getDocker_name());
                // remove it from executingList
                synchronized (usedDockerPool.getExecutingList()) {
                    usedDockerPool.getExecutingList().remove(docker_selected);
                }
            }
        }

        // TODO: not trigger or query case ERROR
        if (response == null){
            response = new Judge.QUERY_RESULT(-2, -1, "", "");
        }

        int score = response.getScore();
        double real_score = (double)response.getScore();
        int status = 0;
        log.info("score: " + score);
        if (score > 0 && score < 100) {
            score = -3;
        }
        switch (score){
            case 100: status = 1;  break; // accept
            case 0:   status = -1; break; // wrong
            case -1:  status = -2; break; // 提交的sql 跑出 exception
            case -3:  status = -3; break; // 对一部分
            case -2:
            case -4:
                status = -4;  break; // 后端判题出现异常 请稍后再试
            default: status = -5; break; // 无效提交
        }
        double running_time = response.getExec_time();
        if (status != 1){
            running_time = -1;
        }
        // TODO: New Change here
//        recordMapper.setRecordStatus(record_id, status, running_time);
        recordMapper.setRecordStatusNScore(record_id, status, running_time, real_score);
        log.info("判题结束 " + "current_size_after_judge: " + usedDockerPool.getRunningList().size());
    }

    private int getSqlTypeIndex(String string_sql_type){
        if (string_sql_type.equals("sqlite")){
            return 1;
        }
        else if(string_sql_type.equals("mysql")){
            return 2;
        }
        else {
            return 0;
        }
    }


}
