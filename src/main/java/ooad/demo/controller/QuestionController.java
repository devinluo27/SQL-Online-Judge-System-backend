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
import org.springframework.validation.annotation.Validated;
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

    @AccessLimit(maxCount = 10, seconds = 3)
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
     *
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
     *     private Boolean is_visible; false invisible or true visible
     *
     *     private Integer operation_type; "query": 1 or "trigger": 2
     *
     *     @NotNull
     *     private Boolean is_order; true or false
     * @param question
     */
    @PostMapping("/admin/addQuestion")
    public void addQuestion(@RequestBody Question question, HttpServletResponse response) throws IOException {
        System.out.println(question.getQuestion_output());
        System.out.println(question.getOperation_type());

        if (!question.getOperation_type().equals("query")){
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to create this query question!");
            return;
        }
        try {
            questionMapper.addQuestion(question);
        }catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to create this query question!");
            return;
        }
        ResultTool.writeResponseFail(response);
    }

    @PostMapping("/admin/addQuestionTrigger")
    public void addQuestionTrigger(@RequestBody Question question,
                                   @RequestParam(value = "ans_table_file_id") Integer ans_table_file_id,
                                   @RequestParam(value = "test_data_file_id") Integer test_data_file_id,
                                   @RequestParam(value = "target_table") String target_table,
                                   @RequestParam(value = "test_config") String test_config,
                                   HttpServletResponse response) throws IOException {
        if (!question.getOperation_type().equals("trigger")){
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to create this trigger question!");
            return;
        }
        try {
            questionMapper.addQuestion(question);
            int question_id = question.getQuestion_id();
            QuestionTrigger questionTrigger = new QuestionTrigger(question_id, ans_table_file_id,
                    test_data_file_id, test_config, target_table);
            questionTriggerMapper.addQuestionTrigger(questionTrigger);
        }catch (Exception e){
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to create this trigger question!");
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    // 将会直接覆盖原有题目!!!
    @PostMapping("/admin/updateQuestion")
    public void updateQuestion(@RequestBody Question question, HttpServletResponse response) throws IOException {
        if (!question.getOperation_type().equals("trigger") && !question.getOperation_type().equals("query")){
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to create this trigger question!");
            return;
        }
        try{
            questionMapper.updateQuestion(question);
        } catch (Exception e){
            ResultTool.writeResponseFailWithData(response,ResultCode.COMMON_FAIL, "Failed to update this query question!");
        }
        ResultTool.writeResponseSuccess(response);
    }


    @GetMapping("/admin/deleteQuestion")
    public void deleteQuestion(@RequestParam(value = "question_id") Integer question_id,
                               HttpServletResponse response){
        if (questionMapper.disableQuestion(question_id) == 1){
            ResultTool.writeResponseSuccess(response);
            return;
        }
        ResultTool.writeResponseFail(response);
    }

}
