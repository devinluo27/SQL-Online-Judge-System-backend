package ooad.demo.mapper;


import ooad.demo.pojo.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface AssignmentMapper {

    List<Assignment> getVisibleAssignmentList();

    List<Assignment> getAllAssignmentList();


    //todo
    Assignment selectAssignmentAllInfoById(int assignment_id);

    Assignment selectAssignmentById(int assignment_id);


    int addAssignment(Assignment assignment);

    int updateAssignment(Assignment assignment);

    Assignment queryQuestionsByAssignment(int assignment_id);

    Assignment queryAssignmentSid(int sid, int assignment_id);

    int deleteAssignment(int assignment_id);

}
