package ooad.demo.mapper;

import ooad.demo.pojo.Question;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuestionMapper {
    List<Question> queryQuestionList();
    Question selectQuestionById(int question_id);
    List<Question> queryQuestionByAssignment(int assignment_id);
    int addQuestion(Question question);
    int updateQuestion(Question question);
    int deleteQuestion(int d);

}
