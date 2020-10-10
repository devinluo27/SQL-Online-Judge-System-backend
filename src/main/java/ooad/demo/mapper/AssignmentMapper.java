package ooad.demo.mapper;


import ooad.demo.pojo.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface AssignmentMapper {
    List<Assignment> queryAssignmentList();

    Assignment selectAssignmentById(int id);

    int addAssignment(Assignment assignment);

    int updateAssignment(Assignment assignment);

    int deleteAssignment(int id);

}
