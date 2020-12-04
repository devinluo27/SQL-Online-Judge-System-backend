package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import ooad.demo.Service.JudgeService;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class RecordController{

    private final RecordMapper recordMapper;

    final JudgeService judgeService;

    private final QuestionMapper questionMapper;

    private final UserController userController;

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


    @CrossOrigin
    @GetMapping("/user/selectRecordByAssignment")
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

//    @CrossOrigin
//    @GetMapping("/user/addRecord")
//    int addRecord(int sid, int question_id, String code, String type){
//        try {
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//    //        String standard = "select * from countries";
//    //        questionMapper.selectQuestionById(question_id);
//            String standard = questionMapper.getStandardAns(question_id).getQuestion_standard_ans();
//    //        System.out.println(standard);
//            int status = judgeCode(standard, code);
//            return recordMapper.addRecord(sid, question_id, status, timestamp, code, type);
//        } catch (Exception e){
//            // 提交失败
//            return -1;
//        }
//    }



    @CrossOrigin
    @GetMapping("/admin/deleteRecordByRid")
    int deleteARecordByRid(@RequestParam(value = "record_id") Integer record_id){
        return recordMapper.deleteARecord(record_id);
    }


    @CrossOrigin
//    @GetMapping("/runSql")
    List<LinkedHashMap<String, Object>> runSql(String code){
        List<LinkedHashMap<String, Object>> list = recordMapper.runSql(code);
        String[] ans = new String[list.size()];
        int i = 0;
        for (LinkedHashMap<String, Object> map : list) {
            for(Object o: map.values()){
                ans[i] += String.valueOf(o);
            }
            i++;
        }
        return list;
    }

    @CrossOrigin
//    @GetMapping("/judgeCode")
    int judgeCode(String standard, String code) {
        List<LinkedHashMap<String, Object>> a;
        try {
            a = recordMapper.judge(standard, code);
        }catch (DataAccessException e){
            System.out.println(e);
            return -1;
        }
        return a.size() == 0 ? 1 : 0;
    }

//    @GetMapping("/judge")
//    public int judgeCodeDocker(String question_id,  String standard_ans, String code, boolean isOrder, int operation_type) throws IOException, JSchException {
//        Random random = new Random();
//        if (ManageDockers.getInstance().getDockersHashMap().get(question_id) == null){
//            synchronized (ManageDockers.getInstance().getDockersHashMap()){
//                if (ManageDockers.getInstance().getDockersHashMap().get(question_id) == null ){
//                    HashMap<String, DockerPool> map  =  ManageDockers.getInstance().getDockersHashMap();
//                    map.put(question_id,
//                            new DockerPool(5, random.nextInt(100000) , 0,"film",
//                                    "/data/xiangjiahong/project/DBOJ/DockerTest/film.sql"));
//                }
//            }
//        }
//        HashMap<String, DockerPool> map  =  ManageDockers.getInstance().getDockersHashMap();
//        ArrayList<String> dockers = (map.get(question_id)).getRunningList();
//        int rand = random.nextInt(dockers.size());
//        String dockID = dockers.get(rand);
//        System.out.println(dockID);
//        Judge.QUERY_RESULT response =  Judge.EXEC_QUERY(standard_ans, code, dockID, isOrder, 0);
//        return response.score;
//    }



    @CrossOrigin
    @PostMapping("/user/addRecord")
    public void addRecord(@RequestParam(value = "question_id") int question_id,
                   @RequestParam(value = "code") String code,
                   @RequestParam(value = "type") String type,
                   HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException, JSchException {

        httpServletResponse.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Question q = questionMapper.getStandardAns(question_id);
        String standard_ans = q.getQuestion_standard_ans();
        Integer database_id = q.getDatabase_id();
        Record r = new Record(sid, question_id, 2, code, type);
        // add record first
        int is_success = recordMapper.addRecord(r);
        int record_id = r.getRecord_id();
        // add to docker to judge
        System.out.println(r.getRecord_id());
        submitToDocker(record_id, String.valueOf(database_id), standard_ans, code,  type);

        try {
//            status = judgeCode(standard_ans, code);
//            setRecordStatus(sid, "1", standard_ans, code,  type);
        } catch (Exception e){
            System.out.println();
//            recordMapper.addRecord(sid, question_id, -1, timestamp, code, type);
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            System.out.println(e.fillInStackTrace());
            System.out.println(e.getMessage());
            // 塞到HttpServletResponse中返回给前台
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }
//        recordMapper.addRecord(sid, question_id, status, timestamp, code, type);
        JsonResult result = ResultTool.success();
        httpServletResponse.getWriter().write(JSON.toJSONString(result));

    }

    public void submitToDocker(int record_id, String question_id, String standard_ans, String code, String type) throws IOException, JSchException {
//        System.out.println(judgeService);
//        System.out.println(record_id +"\n" + question_id + standard_ans + code + type);
        Integer status = judgeService.judgeCodeDocker(record_id, question_id, standard_ans, code, false, 1, type);
    }

}
