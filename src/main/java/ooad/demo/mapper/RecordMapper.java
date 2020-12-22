package ooad.demo.mapper;

import ooad.demo.pojo.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

@Mapper
@Repository
public interface RecordMapper {
    List<Record> queryAllRecordList(Integer page_num);

    List<Record> selectRecordListBySid(int sid);

    Record selectRecordBySidAndQuestion(int sid, int question_id);

    Record selectARecordById(Integer record_id, Integer sid);


    List<LinkedHashMap<String, Object>> runSql(String code);

    List<LinkedHashMap<String, Object>> judge(String standard, String code);

    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id);

    List<Record> selectLatestRecordByQuestionId(int question_id);


    int addRecord(Record record);
    int deleteARecord(Integer record_id);
    int setRecordStatus(Integer record_id, Integer record_status, Double running_time);
}
