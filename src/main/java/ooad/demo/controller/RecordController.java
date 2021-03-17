package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.Service.DockerPoolService;
import ooad.demo.Service.JudgeService;
import ooad.demo.utils.AccessLimit;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/***
 *  GetMapping("/admin/queryAllRecordListInfo")
 *  GetMapping("/user/selectRecordListBySid")
 *  GetMapping("/user/selectRecordBySidAndAssignment")
 *
 *  PostMapping("/user/addRecord")
 */

@Slf4j
@RestController
public class RecordController{

    private final RecordMapper recordMapper;

    final JudgeService judgeService;

    private final QuestionMapper questionMapper;

    private final UserController userController;

    private final int PENDING = 0;

    private final String query = "_query";

    private final String trigger = "_trigger";

    @Autowired
    private DockerPoolService dockerPoolService;


    public RecordController(RecordMapper recordMapper, JudgeService judgeService, QuestionMapper questionMapper, UserController userController) {
        this.recordMapper = recordMapper;
        this.judgeService = judgeService;
        this.questionMapper = questionMapper;
        this.userController = userController;
    }

    @GetMapping("/admin/queryAllRecordListInfo")
    List<Record> queryRecordList(@RequestParam("page_num") Integer page_num){
        return recordMapper.queryAllRecordList(page_num);
    }

    @CrossOrigin
    @GetMapping("/user/selectRecordListBySid")
    List<Record> selectRecordListBySid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/json;charset=utf-8");
            if (request.getUserPrincipal() == null){
                JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
                response.getWriter().write(JSON.toJSONString(result));
                return null;
            }
            int sid = Integer.parseInt(request.getUserPrincipal().getName());
            return recordMapper.selectRecordListBySid(sid);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/user/selectRecordById")
    Record selectRecordById(@RequestParam(value = "record_id") int record_id,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return recordMapper.selectARecordById(record_id, sid);
    }


    @GetMapping("/user/selectRecordBySidAndAssignment")
    List<Record> selectRecordBySidAndAssignment(
            @RequestParam(value = "assignment_id") int assignment_id,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException{
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return recordMapper.selectRecordBySidAndAssignment(sid, assignment_id);
    }

    // TODO
    @GetMapping("/admin/deleteRecordByRid")
    int deleteARecordByRid(@RequestParam(value = "record_id") Integer record_id){
        return recordMapper.deleteARecord(record_id);
    }


    /***
     *  status = 1 -> accept
     *  status = 0 -> pending
     *  status = -1 -> wrong
     *  status = -2 -> exception
     * @param question_id
     * @param code
     * @param
     * @param request
     * @param response
     * @throws IOException
     */

    @AccessLimit(maxCount = 3, seconds = 3)
    @PostMapping("/user/addRecord")
    public void addRecord(
//            @Validated  @RequestBody Record record,
            @RequestParam(value = "question_id") Integer question_id,
                   @RequestParam(value = "code") String code,
//                   @RequestParam(value = "type") String sql_type,
                   HttpServletRequest request, HttpServletResponse response) throws IOException {
//        System.out.println(request.getHeader("Content-Type"));
//        int question_id = record.getRecord_question_id();
//        String code = record.getRecord_code();
//        String sql_type = record.getRecord_code_type();
//        System.out.println(request.isUserInRole("admin"));

        if (request.getUserPrincipal() == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN);
            return;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        // get question details for judge machine
        Question question = questionMapper.getInfoForJudge(question_id);
        if (question == null){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "Rejected! No such question!");
            log.info(request.getUserPrincipal().getName(), "Submit to a wrong question!");
        }

        // Check DDL
//        if (!(checkDDL(question_id) && checkIsQuestionAvailable(question))){
//            ResultTool.writeResponseFail(response, ResultCode.CANNOT_SUBMIT);
//            return;
//        }

        Record r = new Record(sid, question_id, PENDING, code, question.getQuestion_sql_type());
        // add record first, set to PENDING Status
        recordMapper.addRecord(r);
        int record_id = r.getRecord_id();
        try {
            // Docker Judge Function
            submitToDocker(record_id, question, code);
        } catch (Exception e){
            e.printStackTrace();
            log.error("Add Record ", e);
            // 塞到HttpServletResponse中返回给前台
            ResultTool.writeResponseFail(response, ResultCode.PARAM_TYPE_ERROR);
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    /***
     *  如果没过DDL return true; 已过 return false
     * @param question_id
     * @return
     */
    private boolean checkDDL(int question_id){
        Timestamp ddl = questionMapper.getDDL(question_id);
        if (ddl == null) return false;
        return ddl.getTime() - System.currentTimeMillis() >= 0;
    }

    private boolean checkIsQuestionAvailable(Question question){
        return question.getIs_visible() && question.getIs_enabled();
    }

    /***
     * 该函数将学生代码提交到Docker中
     * @param record_id 该条record在数据库中的id
     * @param question 需要判的题
     * @param code 学生代码
     * @throws IOException
     * @throws JSchException
     */
    private void submitToDocker(int record_id,  Question question, String code) throws IOException, JSchException {
        // 什么sql语言与建dockerPool无关
        String dockerPoolMapKey = dockerPoolService.InitDockerPool(question.getDatabase_id(), question.getOperation_type());
        // Trigger Judge will remove a docker
        // add a docker if it is a trigger question
        if(question.getOperation_type().equals("trigger")){
            dockerPoolService.createADocker(ManageDockersPool.getInstance().getDockersPoolHashMap().get(dockerPoolMapKey));
        }
        else {
            // query 判题删掉分号
//            int index = code.lastIndexOf(',');
            code = code.replace(';', ' ').trim();
        }
        judgeService.judgeCodeDocker(record_id, question, code);
    }

    @GetMapping("/admin/getDockerPoolSize")
    public void getDockerPoolSize(int database_id, HttpServletResponse response){
        int sizeQuery = ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id + query).getRunningList().size();
        int sizeTrigger = ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id + trigger).getRunningList().size();
        String ret_data = ("sizeQuery: " + sizeQuery + ".\nsizeTrigger: " + sizeTrigger);
        ResultTool.writeResponseSuccessWithData(response, ret_data);
    }


    /***
     * rejudge  the last submitted record of the given question_id
     * @param question_id
     */
    @GetMapping("/admin/rejudgeByQuestionId")
    public void rejudgeByQuestionId(
            @RequestParam(value = "question_id") Integer question_id,
            HttpServletResponse response) throws IOException, JSchException {
        List<Record> rejudgeList = recordMapper.selectLatestRecordByQid(question_id);
        Question question = questionMapper.getInfoForJudge(question_id);
        try {
            // Docker Judge Function
            for (Record r : rejudgeList){
                Record newRecord = new Record(r.getRecord_sid(), question_id, PENDING, r.getRecord_code(), question.getQuestion_sql_type());
                recordMapper.addRecord(newRecord);
                submitToDocker(newRecord.getRecord_id(), question, r.getRecord_code());
                Thread.sleep(10);
            }
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.JUDGE_FAIL);
            log.error("rejudgeByQuestionID: ",e);
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }






//    ==================  ==================  ==================   ==================  ==================

    // for testing only
    @Async
    public void addRecord(
//            @RequestBody Record record,
            @RequestParam(value = "question_id") int question_id,
            @RequestParam(value = "code") String code,
            @RequestParam(value = "type") String sql_type
            ) {

//        int question_id = record.getRecord_question_id();
//        String code = record.getRecord_code();
//        String sql_type = record.getRecord_code_type();

        code = code.replace(';', ' ').trim();

        int sid = 1;
        Question q = questionMapper.getInfoForJudge(question_id);
        String standard_ans = q.getQuestion_standard_ans();

        // get question details for judge machine
        Integer database_id = q.getDatabase_id();
        String operation_type = q.getOperation_type();

        Record r = new Record(sid, question_id, PENDING, code, sql_type);
        // add record first PENDING Status
        recordMapper.addRecord(r);
        int record_id = r.getRecord_id();
        try {
            // Docker Judge Function
            submitToDocker(record_id, q, code);
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            e.printStackTrace();
            // 塞到HttpServletResponse中返回给前台
        }
    }

}
