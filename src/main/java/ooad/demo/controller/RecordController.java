package ooad.demo.controller;

import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

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
    int addRecord(int sid, int question_id, String code, String type){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String standard = "select * from record";
        int status = judgeCode(standard, code);
        return recordMapper.addRecord(sid, question_id, status, timestamp, code, type);
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

    @CrossOrigin
    @GetMapping("/judgeCode")
    int judgeCode(String standard, String code) {
        List<LinkedHashMap<String, Object>> a;
        try {
            a = recordMapper.judge(standard, code);
        }catch (DataAccessException e){
            return -1;
        }
        return a.size() == 0 ? 1 : 0;
    }

    int setRecordStatus(int sid, int question_id, int status){
        return 0;
    }
}
