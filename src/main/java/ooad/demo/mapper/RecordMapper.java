package ooad.demo.mapper;

import ooad.demo.pojo.Record;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RecordMapper {
    List<Record> queryRecordList();
    List<Record> selectRecordBySid(int sid);
    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id);
    int qeeRecord(int sid, int question_id, String code);
    int deleteARecord(int sid, int question_id);
    int setRecordStatus(int sid, int question_id, int status);
}
