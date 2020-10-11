package ooad.demo.controller;



import ooad.demo.mapper.QuestionMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
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
}
