package ooad.demo.controller;



import ooad.demo.mapper.QuestionMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
public class QuestionController implements Serializable {
    @Autowired
    private QuestionMapper questionMapper;

    @CrossOrigin
    @GetMapping("/queryQuestionList")
    public List<Question> queryQuestionList(){
        return questionMapper.queryQuestionList();
    }

    @CrossOrigin
    @GetMapping("/selectQuestionsById")
    public Question selectQuestionsById(String id){
        int question_id;
        try{
            question_id = Integer.parseInt(id);
        } catch (Exception e){
            return null;
        }
        Question ret =  questionMapper.selectQuestionById(question_id);
//        System.out.println(ret.getQuestion_output());
        return ret;
    }


    @CrossOrigin
    @GetMapping("/selectQuestionsByAssignment")
    public List<Question>  selectQuestionsByAssignment(String sid, String as_id){
        int student_id;
        int assignment_id;
        try{
            student_id = Integer.parseInt(sid);
            assignment_id = Integer.parseInt(as_id);
        } catch (Exception e){
            return null;
        }
        List<Question> list = questionMapper.selectQuestionsByAssignment(student_id, assignment_id);
        return list;
    }
}
