package ooad.demo.Service;


import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockers;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Service
public class JudgeService {
    @Autowired
    RecordMapper recordMapper;

    @Async
    public Integer judgeCodeDocker(int sid, String question_id,  String standard_ans, String code, boolean isOrder, int operation_type, String type) throws IOException, JSchException {
        Random random = new Random();
        if (ManageDockers.getInstance().getDockersHashMap().get(question_id) == null){
            synchronized (ManageDockers.getInstance().getDockersHashMap()){
                if (ManageDockers.getInstance().getDockersHashMap().get(question_id) == null ){
                    HashMap<String, DockerPool> map  =  ManageDockers.getInstance().getDockersHashMap();
                    map.put(question_id,
                            new DockerPool(5, random.nextInt(100000) , 0,"film",
                                    "/data/xiangjiahong/project/DBOJ/DockerTest/film.sql"));
                }
            }
        }
        HashMap<String, DockerPool> map  =  ManageDockers.getInstance().getDockersHashMap();
        ArrayList<String> dockers = (map.get(question_id)).getRunningList();
        int rand = random.nextInt(dockers.size());
        String dockID = dockers.get(rand);
        System.out.println(dockID);
        Judge.QUERY_RESULT response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);
        int status = response.score == 100 ?  1 : 0;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//        System.out.println("status: " + status);
        recordMapper.addRecord(sid, Integer.parseInt(question_id), status, currentTime, code, type);
        return response.score;
    }
}
