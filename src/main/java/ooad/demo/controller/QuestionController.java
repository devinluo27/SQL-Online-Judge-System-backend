package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import ooad.demo.utils.AccessLimit;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.QuestionTriggerMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.QuestionTrigger;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private QuestionTriggerMapper questionTriggerMapper;


    @AccessLimit(maxCount = 3, seconds = 10)
    @GetMapping("/admin/queryQuestionList")
    public List<Question> queryQuestionList(){
        System.out.println("queryQuestionList");
        return questionMapper.queryQuestionList();
    }

    @GetMapping("/user/selectQuestionsById")
    public Question selectQuestionsById(@RequestParam(value = "question_id") Integer question_id){
        return questionMapper.selectQuestionById(question_id);
    }

    @AccessLimit(maxCount = 3, seconds = 100)
    @GetMapping("/user/selectQuestionsByAssignment")
    public List<Question>  selectQuestionsByAssignment(
            @RequestParam(value = "assignment_id") int assignment_id,
            HttpServletRequest request,
            HttpServletResponse response){
        if (request.getUserPrincipal() == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN );
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return questionMapper.selectQuestionsByAssignment(sid, assignment_id);
    }

    /***
     *     private Integer id;
     *     @NotNull
     *     private Integer question_id;
     *
     *     @NotNull
     *     private String question_name;
     *
     *     @NotNull
     *     private Integer question_of_assignment;
     *
     *     @NotNull
     *     private String question_description;
     *
     *     @NotNull
     *     private String question_output;
     *
     *     @NotNull
     *     private Integer question_index;
     *
     *     private Integer is_finished; not for teachers
     *
     *     @NotNull
     *     private String question_standard_ans;
     *
     *     @NotNull
     *     private Integer database_id;
     *
     *     @NotNull
     *     private Integer is_visible; 0 invisible or 1 visible
     *
     *     private Integer operation_type; "query": 1 or "trigger": 2
     *
     *     @NotNull
     *     private Boolean is_order; true or false
     * @param question
     */
    @PostMapping("/admin/addQuestion")
    public void addQuestion(@RequestBody Question question, HttpServletResponse response) throws IOException {
        try {
            questionMapper.addQuestion(question);
            int question_id = question.getQuestion_id();
        }catch (Exception e){
            JsonResult result = ResultTool.fail();
            result.setData("Failed to create this query question!");
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        JsonResult result = ResultTool.success();
        response.getWriter().write(JSON.toJSONString(result));

    }

    @PostMapping("/admin/addQuestionTrigger")
    public void addQuestionTrigger(@RequestBody Question question,
                                   @RequestParam(value = "ans_table_file_id") Integer ans_table_file_id,
                                   @RequestParam(value = "test_data_file_id") Integer test_data_file_id,
                                   @RequestParam(value = "target_table") String target_table,
                                   @RequestParam(value = "test_config") String test_config,
                                   HttpServletResponse response) throws IOException {
        try {
            questionMapper.addQuestion(question);
            int question_id = question.getQuestion_id();
            QuestionTrigger questionTrigger = new QuestionTrigger(question_id, ans_table_file_id,
                    test_data_file_id, test_config, target_table);
            questionTriggerMapper.addQuestionTrigger(questionTrigger);
        }catch (Exception e){
            JsonResult result = ResultTool.fail();
            result.setData("Failed to create this trigger question!");
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        JsonResult result = ResultTool.success();
        response.getWriter().write(JSON.toJSONString(result));
    }

    // 将会直接覆盖原有题目!!!
    @PostMapping("/admin/updateQuestion")
    public void updateQuestion(@RequestBody Question question, HttpServletResponse response) throws IOException {
        try{
            questionMapper.updateQuestion(question);
        } catch (Exception e){
            JsonResult result = ResultTool.fail();
            result.setData("Failed to create this trigger question!");
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        JsonResult result = ResultTool.success();
        response.getWriter().write(JSON.toJSONString(result));
    }

    @GetMapping("/admin/deleteQuestion")
    public void deleteQuestion(@RequestParam(value = "question_id") Integer question_id){
        questionMapper.disableQuestion(question_id);
    }



}
