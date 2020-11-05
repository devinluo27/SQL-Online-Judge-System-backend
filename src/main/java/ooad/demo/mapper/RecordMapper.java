package ooad.demo.mapper;

import ooad.demo.pojo.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

@Mapper
@Repository
public interface RecordMapper {
    List<Record> queryRecordList();
    List<Record> selectRecordBySid(int sid);

    Record selectRecordBySidAndQuestion(int sid, int question_id);

//    @Select("${code}")
//    List<LinkedHashMap<String, Object>> runSql(@Param(value="code") String code);

    List<LinkedHashMap<String, Object>> runSql(String code);

    List<LinkedHashMap<String, Object>> judge(String standard, String code);

    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id);
    int addRecord(int sid, int question_id, int record_status, Timestamp time, String code, String type);
    int deleteARecord(int sid, int question_id);
    int setRecordStatus(int sid, int question_id, int status);
}
