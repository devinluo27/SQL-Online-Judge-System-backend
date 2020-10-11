package ooad.demo.controller;

import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RecordController implements Serializable {
    @Autowired
    private RecordMapper recordMapper;

    @CrossOrigin
    @GetMapping("/queryRecordList")
    List<Record> queryRecordList(){
        return recordMapper.queryRecordList();
    }

    @CrossOrigin
    @GetMapping("/selectRecordBySid")
    List<Record> selectRecordBySid(int sid){
        return recordMapper.selectRecordBySid(sid);
    }

    @CrossOrigin
    @GetMapping("/selectRecordBySidAndAssignment")
    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id){
        return recordMapper.selectRecordBySidAndAssignment(sid, assignment_id);
    }

    @CrossOrigin
    @GetMapping("/addRecord")
    int addRecord(int sid, int question_id, String code){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        deleteARecord(sid, question_id);
        return recordMapper.addRecord(sid, question_id, timestamp, code);
    }

    @CrossOrigin
    @GetMapping("/deleteRecord")
    int deleteARecord(int sid, int question_id){
        return recordMapper.deleteARecord(sid, question_id);
    }

    @CrossOrigin
    @GetMapping("/runSql")
    List<LinkedHashMap<String, Object>> runSql(String code){
        List<LinkedHashMap<String, Object>> list = recordMapper.runSql(code);
        String[] ans = new String[list.size()];
        int i = 0;
        for (LinkedHashMap<String, Object> map : list) {
            for(Object o: map.values()){
                ans[i] += String.valueOf(o);
            }
            i++;
        }
        return list;
    }

    int setRecordStatus(int sid, int question_id, int status){
        return 0;
    }
}
