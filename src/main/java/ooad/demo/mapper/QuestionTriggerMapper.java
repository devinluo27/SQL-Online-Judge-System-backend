package ooad.demo.mapper;

import ooad.demo.pojo.QuestionTrigger;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionTriggerMapper {
    QuestionTrigger getQuestionTriggerByQid();
    int addQuestionTrigger(QuestionTrigger questionTrigger);
}
