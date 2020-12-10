package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import ooad.demo.Service.FillDockerPoolService;
import ooad.demo.Service.InitDockerPoolService;
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

    @Autowired
    private FillDockerPoolService fillDockerPoolService;

    @Autowired
    private InitDockerPoolService initDockerPoolService;

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
//            @RequestBody Record record,
            @RequestParam(value = "question_id") int question_id,
                   @RequestParam(value = "code") String code,
                   @RequestParam(value = "type") String sql_type,
                   HttpServletRequest request, HttpServletResponse response) throws IOException {

//        int question_id = record.getRecord_question_id();
//        String code = record.getRecord_code();
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
            submitToDocker(record_id, question_id, database_id, code, sql_type);
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

    public void submitToDocker(int record_id, Integer question_id, Integer database_id, String code,
                               String sql_type) throws IOException, JSchException {

        initDockerPoolService.InitDockerPool(database_id);
        fillDockerPoolService.createADocker(ManageDockersPool.getInstance().getDockersPoolHashMap().get(String.valueOf(database_id)));

        judgeService.judgeCodeDocker(record_id, question_id, code, false, sql_type);

    }

    @GetMapping("admin/getDockerPoolSize")
    public void getDockerPoolSize(int database_id, HttpServletResponse response) throws IOException {
        JsonResult<String> result = ResultTool.success();
        int size = ManageDockersPool.getInstance().getDockersPoolHashMap().get(String.valueOf(database_id)).getRunningList().size();
        result.setData("size: " + size);
        response.getWriter().write(JSON.toJSONString(result));
    }

    //    @CrossOrigin
//    @GetMapping("/judgeCode")
//    int judgeCode(String standard, String code) {
//        List<LinkedHashMap<String, Object>> a;
//        try {
//            a = recordMapper.judge(standard, code);
//        }catch (DataAccessException e){
//            System.out.println(e);
//            return -1;
//        }
//        return a.size() == 0 ? 1 : 0;
//    }

//    @CrossOrigin
////    @GetMapping("/runSql")
//    List<LinkedHashMap<String, Object>> runSql(String code){
//        List<LinkedHashMap<String, Object>> list = recordMapper.runSql(code);
//        String[] ans = new String[list.size()];
//        int i = 0;
//        for (LinkedHashMap<String, Object> map : list) {
//            for(Object o: map.values()){
//                ans[i] += String.valueOf(o);
//            }
//            i++;
//        }
//        return list;
//    }
    @Async
    public void addRecord(
//            @RequestBody Record record,
            @RequestParam(value = "question_id") int question_id,
            @RequestParam(value = "code") String code,
            @RequestParam(value = "type") String sql_type
            ) throws IOException {

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
            submitToDocker(record_id, question_id, database_id, code, sql_type);
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            System.out.println(e.fillInStackTrace());
            System.out.println(e.getMessage());
            // 塞到HttpServletResponse中返回给前台
        }
    }

}
