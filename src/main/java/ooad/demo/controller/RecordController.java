package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import ooad.demo.Service.JudgeService;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
import ooad.demo.judge.DockerPool;
import ooad.demo.judge.Judge;
import ooad.demo.judge.ManageDockers;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;


@RestController
public class RecordController{
    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    JudgeService judgeService;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserController userController;

    @CrossOrigin
    @GetMapping("/admin/queryAllRecordListInfo")
    List<Record> queryRecordList(){
        return recordMapper.queryRecordList();
    }

    @CrossOrigin
    @GetMapping("/user/selectRecordInfoBySid")
    List<Record> selectRecordBySid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return null;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        return recordMapper.selectRecordBySid(sid);
    }

//    @CrossOrigin
//    @GetMapping("/user/selectRecordInfoBySid")
//    List<Record> selectRecordByRid(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//    }


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
    @GetMapping("/admin/deleteRecord")
    int deleteARecord(int sid, int question_id){
        return recordMapper.deleteARecord(sid, question_id);
    }

    @CrossOrigin
    @GetMapping("/admin/deleteRecordByRid")
    int deleteARecordByRid(int sid, int question_id){
        return recordMapper.deleteARecord(sid, question_id);
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
    void addRecord(@RequestParam(value = "question_id") int question_id,
                   @RequestParam(value = "code") String code,
                   @RequestParam(value = "type") String type,
                   HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException, JSchException {
        httpServletResponse.setContentType("text/json;charset=utf-8");
//        System.out.println(request.getCookies());

        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Question q = questionMapper.getStandardAns(question_id);

        String standard_ans = q.getQuestion_standard_ans();
        System.out.println("s ans: " + standard_ans);
        int status = 0;
        setRecordStatus(sid, "1", standard_ans, code,  type);

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

    public void setRecordStatus(int sid, String question_id, String standard_ans, String code, String type) throws IOException, JSchException {
        int status = judgeService.judgeCodeDocker(sid, "1", standard_ans, code, false, 1, type) == 100 ? 1 : 0;
//        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        System.out.println("status: " + status);
//        recordMapper.addRecord(sid, Integer.parseInt(question_id), status, currentTime, code, type);
    }

}
