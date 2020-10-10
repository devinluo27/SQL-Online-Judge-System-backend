package ooad.demo.mapper;


import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface AssignmentMapper {
    List<Assignment> queryAssignmentList();

    Assignment selectAssignmentById(int id);

    int addAssignment(Assignment assignment);

    int updateAssignment(Assignment assignment);

    Assignment queryQuestionsByAssignment(int id);
    int deleteAssignment(int id);

}
