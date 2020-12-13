package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import ooad.demo.Service.DockerPoolService;
import ooad.demo.Service.JudgeService;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
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
import java.util.List;

/***
 *  GetMapping("/admin/queryAllRecordListInfo")
 *  GetMapping("/user/selectRecordListBySid")
 *  GetMapping("/user/selectRecordBySidAndAssignment")
 *
 *  PostMapping("/user/addRecord")
 */
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

    @CrossOrigin
    @GetMapping("/admin/queryAllRecordListInfo")
    List<Record> queryRecordList(@RequestParam("page_num") Integer page_num){
        return recordMapper.queryAllRecordList(page_num);
    }

    @CrossOrigin
    @GetMapping("/user/selectRecordListBySid")
    List<Record> selectRecordListBySid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return recordMapper.selectRecordListBySid(sid);
    }

    @GetMapping("/user/selectRecordById")
    Record selectRecordById(@RequestParam(value = "record_id") int record_id,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return recordMapper.selectARecordById(record_id, sid);
    }

    @CrossOrigin
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
    @CrossOrigin
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
     * @param sql_type
     * @param request
     * @param response
     * @throws IOException
     */
    @CrossOrigin
    @PostMapping("/user/addRecord")
    public void addRecord(
            @RequestBody Record record,
            @RequestParam(value = "question_id") int question_id,
                   @RequestParam(value = "code") String code,
                   @RequestParam(value = "type") String sql_type,
                   HttpServletRequest request, HttpServletResponse response) throws IOException {

//        int question_id = record.getRecord_question_id();
         code = record.getRecord_code();
//        String sql_type = record.getRecord_code_type();

        code = code.replace(';', ' ').trim();

        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        Question q = questionMapper.getInfoForJudge(question_id);

        // get question details for judge machine

        Record r = new Record(sid, question_id, PENDING, code, sql_type);
        // add record first PENDING Status
        recordMapper.addRecord(r);
        int record_id = r.getRecord_id();
        try {
            // Docker Judge Function
            submitToDocker(record_id, question_id, q, code, sql_type);
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            System.out.println(e.fillInStackTrace());
            System.out.println(e.getMessage());
            // 塞到HttpServletResponse中返回给前台
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        JsonResult result = ResultTool.success();
        response.getWriter().write(JSON.toJSONString(result));

    }

    public void submitToDocker(int record_id, Integer question_id, Question question, String code,
                               String sql_type) throws IOException, JSchException {

        String dockerPoolMapKey = dockerPoolService.InitDockerPool(question.getDatabase_id(), question.getOperation_type());
        // Trigger Judge will remove a docker
        // TODO: change
        if(question.getOperation_type() == 2 || question.getOperation_type() == 1){
            dockerPoolService.createADocker(ManageDockersPool.getInstance().getDockersPoolHashMap().get(dockerPoolMapKey));
        }
        judgeService.judgeCodeDocker(record_id, question_id, code, question.getIs_order(), sql_type);
    }

    @GetMapping("/admin/getDockerPoolSize")
    public void getDockerPoolSize(int database_id, HttpServletResponse response) throws IOException {
        JsonResult<String> result = ResultTool.success();
        int sizeQuery = ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id + query).getRunningList().size();
        int sizeTrigger = ManageDockersPool.getInstance().getDockersPoolHashMap().get(database_id + trigger).getRunningList().size();
        result.setData("sizeQuery: " + sizeQuery + "sizeTrigger: " + sizeTrigger);
        response.getWriter().write(JSON.toJSONString(result));
    }

    /***
     * rejudge  the last submitted record of the given question_id
     * @param question_id
     */
    @GetMapping("/admin/rejudgeByQuestionId")
    public void rejudgeByQuestionId(
            @RequestParam(value = "question_id") Integer question_id){

    }


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
        Integer operation_type = q.getOperation_type();

        Record r = new Record(sid, question_id, PENDING, code, sql_type);
        // add record first PENDING Status
        recordMapper.addRecord(r);
        int record_id = r.getRecord_id();
        try {
            // Docker Judge Function
            submitToDocker(record_id, question_id, q, code, sql_type);
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            System.out.println(e.fillInStackTrace());
            System.out.println(e.getMessage());
            // 塞到HttpServletResponse中返回给前台
        }
    }

}
