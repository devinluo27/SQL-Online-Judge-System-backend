package ooad.demo.mapper;

import ooad.demo.pojo.Record;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RecordMapper {
    List<Record> queryAllRecordList(Integer page_num);

    List<Record> selectRecordListBySid(int sid);

    Record selectRecordBySidAndQuestion(int sid, int question_id);

    Record selectARecordById(Integer record_id, Integer sid);




// Todo: temporary comment
    Integer getNumOfRecordBySid(Integer sid);

    List<Record>  selectRecordListBySidNPNum(Integer sid, Integer page_num, Integer page_size);

//    List<LinkedHashMap<String, Object>> runSql(String code);

//    List<LinkedHashMap<String, Object>> judge(String standard, String code);


    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id);

    List<Record> selectLatestRecordByQid(int question_id);

    int addRecord(Record record);

    int deleteARecord(Integer record_id);

    int setRecordStatus(Integer record_id, Integer record_status, Double running_time);

    int setRecordStatusNScore(Integer record_id, Integer record_status, Double running_time, Double score);

    ArrayList<Map<String, Object>> getRecordCountForNDays(int day_num);

    List<Map<String,String>> getLeaderBoardByQidAndN(Integer question_id, Integer num);

//    ArrayList<Record> getLatestRecordByQid(Integer question_id);

}
