package ooad.demo.controller;


import cn.shuibo.annotation.Decrypt;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultTool;
import ooad.demo.mapper.AssignmentMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@RestController
public class AssignmentController {

    @Autowired
    private AssignmentMapper assignmentMapper;

    /***
     * The return msg depends on whether the user has permission "view_all_assignment"
     * not is_visible assignments will be masked.
     * @return
     */
    @CrossOrigin
    @GetMapping("/user/queryAssigmentList")
    public List<Assignment> queryAllAssigmentList(){
        Collection<? extends GrantedAuthority> authorities =  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("view_all_assignment")) {
//                System.out.println("auth: " + authority.getAuthority());
                return  assignmentMapper.getAllAssignmentList();
            }
        }
//        System.out.println("user:");
        // for user without permission
        return assignmentMapper.getVisibleAssignmentList();
    }

    @CrossOrigin
    @GetMapping("/user/selectAssignmentById")
    public Assignment selectAssignment(HttpServletRequest request,
                                       @RequestParam(value = "id") Integer id){
        int assignment_id = id;
        // check something
//        System.out.println("Authority:" + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
//        System.out.println("Credentials:" +  SecurityContextHolder.getContext().getAuthentication().getCredentials());
//        System.out.println("Credentials:" +  SecurityContextHolder.getContext().getAuthentication().getCredentials());
//        System.out.println("Details:" +  SecurityContextHolder.getContext().getAuthentication().getDetails());
        Collection<? extends GrantedAuthority> authorities =  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("view_all_assignment")) {
                System.out.println("auth" + authority.getAuthority());
                return  assignmentMapper.selectAssignmentAllInfoById(assignment_id);
            }
        }
        Assignment assignment = assignmentMapper.selectAssignmentById(assignment_id);

        return  assignment;
    }

    /***
     * pass the id return assignment with associate questions
     * @param assignment_id
     * @return
     */
    @GetMapping("/user/queryQuestionsByAssignmentID")
    public List<Question> queryQuestionsByAssignment(@RequestParam(value = "assignment_id") Integer assignment_id ){
        Assignment assignment = assignmentMapper.queryQuestionsByAssignment(assignment_id);
        return assignment.getQuestions();
    }


    /***
     *
     * @param assignment String assignment_id,
     *                   String assignment_name,
     *                   String assignment_description,
     *                   long assignment_start_time,
     *                   long assignment_end_time,
     *                   int is_visible
     * pass the milisecond from 1970 start_date end_date should be long
     * @param response
     * @throws IOException
     */
    @PostMapping("/admin/addAssignment")
    public void addAssignment(@RequestBody @Validated Assignment assignment, HttpServletResponse response){

        try {
            assignment.setAssignment_create_time(new Timestamp(System.currentTimeMillis()));
            assignmentMapper.addAssignment(assignment);
            ResultTool.writeResponseSuccess(response);
        } catch (Exception e){
            e.printStackTrace();
            log.error("addAssignment: ", e);
            ResultTool.writeResponseFail(response);
            return;
        }
        log.info("add assignment: " + assignment.getAssignment_name());
    }

    /***
     *
     * @param assignment String assignment_id, long assignment_name,
     *          long assignment_start_time, String assignment_end_time,
     *           int is_visible
     * @param id assignment id in database (primary key)
     * @return
     */
    @CrossOrigin
    @PostMapping("/admin/updateAssignment")
    public void updateAssignment(@RequestBody @Validated Assignment assignment,
                                @RequestParam(value = "id") Integer id, HttpServletResponse response) throws IOException {
        try {
            assignment.setId(id);
            assignmentMapper.updateAssignment(assignment);
            ResultTool.writeResponseSuccess(response);
        }
        catch (Exception e){
            ResultTool.writeResponseFail(response);
            e.printStackTrace();
            log.error("update assignment: ", e);
        }

    }


    @GetMapping("/admin/deleteAssignment")
    public void deleteAssignment(
            @RequestParam(value = "assignment_id") Integer assignment_id, HttpServletResponse response){
        try{
            assignmentMapper.deleteAssignment(assignment_id);
            ResultTool.writeResponseSuccess(response);
        } catch (Exception e){
            ResultTool.writeResponseFail(response);
            e.printStackTrace();
            log.error("delete assignment: ", e);
        }
    }


}
