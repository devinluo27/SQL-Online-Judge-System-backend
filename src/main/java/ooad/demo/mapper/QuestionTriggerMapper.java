package ooad.demo.mapper;

import ooad.demo.pojo.QuestionTrigger;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Mapper
@Repository
public interface QuestionTriggerMapper {

    Map<String, String> getTriggerQuestionJudgeInfoByQid(Integer question_id);

    int addQuestionTrigger(QuestionTrigger questionTrigger);

    int updateQuestionTrigger(QuestionTrigger questionTrigger);

}
