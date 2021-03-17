package ooad.demo.controller;

import ooad.demo.judge.RecordPair;
import ooad.demo.judge.SimilarityCheck;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.Record;
import ooad.demo.utils.ResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

@RestController
public class StatisticsController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RecordMapper recordMapper;

    @Autowired
    SimilarityCheck similarityCheck;

    // TODO: NEW URL 54
    @GetMapping("/user/loginCountToday")
    public Integer loginCountToday(HttpServletResponse response){
        return userMapper.getTodayUserLoginCount();
    }

    // TODO: NEW URL 55
    @GetMapping("/user/getRecordCountForAWeek")
    public ArrayList<Map<String, Object>> getRecordCountForAWeek(){
        try{
            return recordMapper.getRecordCountForNDays(7);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // TODO: NEW URL 105
    @GetMapping("/admin/checkSimilarityByQid")
    public Object[] checkSimilarityByQid(@RequestParam(value = "question_id") Integer question_id){
        List<Record> recordList = recordMapper.selectLatestRecordByQid(question_id);
        PriorityBlockingQueue<RecordPair> queue = new PriorityBlockingQueue<>();
        int k = 100;
        for (int i = 0; i < recordList.size() - 1; i++){
            for(int j = i + 1; j < recordList.size(); j++){
                Record s1 = recordList.get(i);
                Record s2 = recordList.get(j);
                double temp_result = similarityCheck.checkTwoCodes(s1.getRecord_code(), s2.getRecord_code());
                RecordPair recordPair = new RecordPair(s1.getRecord_sid(), s2.getRecord_sid(), temp_result);
                if (queue.size() < k){
                    queue.add(recordPair);
                }
                else {
                    double min = queue.peek().getCheckResult();
                    if (min < temp_result){
                        queue.poll();
                        queue.add(recordPair);
                    }
                }
            }
        }
        return queue.toArray();
    }

    // TODO: NEW URL 56
    @GetMapping("/user/getLeaderBoardByQid")
    public void getLeaderBoardByQid(@RequestParam(value = "question_id") Integer question_id,

                                    HttpServletResponse response){
        List<Map<String, String>> return_map;
        try {
            return_map = recordMapper.getLeaderBoardByQidAndN(question_id, 20);
        } catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFail(response);
            return;
        }
        ResultTool.writeResponseSuccessWithData(response, return_map);
    }

}
