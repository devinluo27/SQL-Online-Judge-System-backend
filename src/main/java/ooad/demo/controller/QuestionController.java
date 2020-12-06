package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @GetMapping("/user/selectQuestionsById")
    public Question selectQuestionsById(@RequestParam(value = "question_id") Integer question_id){
        return questionMapper.selectQuestionById(question_id);
    }


    @CrossOrigin
    @GetMapping("/user/selectQuestionsByAssignment")
    public List<Question>  selectQuestionsByAssignment(
            @RequestParam(value = "assignment_id") int assignment_id,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        List<Question> list = questionMapper.selectQuestionsByAssignment(sid, assignment_id);
        return list;
    }
}
