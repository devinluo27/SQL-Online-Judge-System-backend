package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
import ooad.demo.mapper.QuestionMapper;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.pojo.Question;
import ooad.demo.pojo.Record;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;


@RestController
public class RecordController{
    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserController userController;


    @CrossOrigin
    @GetMapping("/queryRecordList")
    List<Record> queryRecordList(){
        return recordMapper.queryRecordList();
    }

    @CrossOrigin
    @GetMapping("/selectRecordBySid")
    List<Record> selectRecordBySid(int sid){
        return recordMapper.selectRecordBySid(sid);
    }

    @CrossOrigin
    @GetMapping("/selectRecordBySidAndAssignment")
    List<Record> selectRecordBySidAndAssignment(int sid, int assignment_id){
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
    @PostMapping("/addRecord")
    void addRecord(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
//        System.out.println(request.getParameter("question_id"));
        httpServletResponse.setContentType("text/json;charset=utf-8");
        System.out.println(request.getCookies());
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            int sid = Integer.parseInt(request.getUserPrincipal().getName());
            int question_id = Integer.parseInt(request.getParameter("question_id"));
            String code = request.getParameter("code");
            String type = request.getParameter("type");
            String standard = questionMapper.getStandardAns(question_id).getQuestion_standard_ans();
            int status = judgeCode(standard, code);
            recordMapper.addRecord(sid, question_id, status, timestamp, code, type);
            JsonResult result = ResultTool.success();
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
        } catch (Exception e){
            JsonResult result = ResultTool.fail(ResultCode.PARAM_TYPE_ERROR);
            System.out.println(e.getStackTrace());
            //塞到HttpServletResponse中返回给前台
            httpServletResponse.getWriter().write(JSON.toJSONString(result));
        }

//        JsonResult result = ResultTool.success();
//        httpServletResponse.setContentType("text/json;charset=utf-8");
//        httpServletResponse.getWriter().write(JSON.toJSONString(result));
    }

    @CrossOrigin
    @GetMapping("/admin/deleteRecord")
    int deleteARecord(int sid, int question_id){
        return recordMapper.deleteARecord(sid, question_id);
    }

    @CrossOrigin
    @GetMapping("/runSql")
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

    int setRecordStatus(int sid, int question_id, int status){
        return 0;
    }
}
