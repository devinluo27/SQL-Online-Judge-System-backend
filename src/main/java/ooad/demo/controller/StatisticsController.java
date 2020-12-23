package ooad.demo.controller;

import ooad.demo.judge.SimilarityCheck;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

@RestController
public class StatisticsController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RecordMapper recordMapper;

    @Autowired
    SimilarityCheck similarityCheck;

    // TODO: NEW URL
    @GetMapping("/user/loginCountToday")
    public Integer loginCountToday(HttpServletResponse response){
        return userMapper.getTodayUserLoginCount();
    }

    // TODO: NEW URL
    @GetMapping("/user/getRecordCountForAWeek")
    public ArrayList<Map<String, Object>> getRecordCountForAWeek(){
        try{
            return recordMapper.getRecordCountForNDays(7);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // TODO: NEW URL
    @GetMapping("/admin/checkSimilarityByQid")
    public void checkSimilarityByQid(@RequestParam(value = "question_id") Integer question_id){
        List<Record> recordList = recordMapper.selectLatestRecordByQid(question_id);
        PriorityBlockingQueue<RecordPair> queue = new PriorityBlockingQueue<>();
        int k = 100;
        for (int i = 0; i < recordList.size() - 1; i++){
            for(int j = i + 1; j < recordList.size(); j++){
                double temp_result = similarityCheck.checkTwoCodes(recordList.get(i).getRecord_code(),recordList.get(j).getRecord_code());
                if (queue.size() < k){
                    queue.add(temp_result);
                }
                else {
                    double min = queue.peek();
                    if (min < temp_result){
                        queue.poll();
                        queue.add(temp_result);
                    }
                }
            }
        }


    }
    private static class RecordPair{
        Integer sid_1 = 0;
        Integer sid_2 = 0;
        Double checkResult = 0.0;

        public RecordPair(Integer sid_1, Integer sid_2, Double checkResult) {
            this.sid_1 = sid_1;
            this.sid_2 = sid_2;
            this.checkResult = checkResult;
        }

        public RecordPair() {
        }

        @Override
        public boolean equals(Object obj) {
            return this.checkResult - (obj).checkResult;
        }
    }



}
