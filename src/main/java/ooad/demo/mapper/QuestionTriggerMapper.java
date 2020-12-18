package ooad.demo.mapper;

import ooad.demo.pojo.QuestionTrigger;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface QuestionTriggerMapper {
    QuestionTrigger getQuestionTriggerByQid();
    int addQuestionTrigger(QuestionTrigger questionTrigger);
}
