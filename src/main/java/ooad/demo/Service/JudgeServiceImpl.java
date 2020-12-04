package ooad.demo.Service;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    RecordMapper recordMapper;

    @Async
    public Integer judgeCodeDocker(int record_id, String question_id,  String standard_ans, String code, boolean isOrder, int operation_type, String type) throws IOException, JSchException {
        Random random = new Random();
        if (ManageDockersPool.getInstance().getDockersHashMap().get(question_id) == null){
            synchronized (ManageDockersPool.getInstance().getDockersHashMap()){
                if (ManageDockersPool.getInstance().getDockersHashMap().get(question_id) == null ){
                    HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersHashMap();
                    map.put(question_id,
                            new DockerPool(1, random.nextInt(100000) , 0,"film",
                                    "/data2/DBOJ/DockerTest/film.sql"));
                }
            }
        }
        HashMap<String, DockerPool> map  =  ManageDockersPool.getInstance().getDockersHashMap();
        ArrayList<String> dockers = (map.get(question_id)).getRunningList();
        int rand = random.nextInt(dockers.size());
        String dockID = dockers.get(rand);
        System.out.println(dockID);
        Judge.QUERY_RESULT response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);
        int status = response.score == 100 ?  1 : 0;
        recordMapper.setRecordStatus(record_id, status);
        return response.score;
    }
}
