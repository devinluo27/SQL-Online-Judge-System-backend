package ooad.demo.controller;


import ooad.demo.mapper.AssignmentMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@RestController
public class AssignmentController {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @CrossOrigin
    @GetMapping("/user/queryAssigmentList")
    public List<Assignment> queryAssigmentList(){
        List<Assignment> assignmentList = assignmentMapper.getVisibleAssignmentList();
        return  assignmentList;
    }

    @CrossOrigin
    @GetMapping("/admin/queryAllAssigmentList")
    public List<Assignment> queryAllAssigmentList(){
        Collection<? extends GrantedAuthority> authorities =  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("modify_assignment")) {
                System.out.println("auth" + authority.getAuthority());

                return  assignmentMapper.getAllAssignmentList();
            }
        }
        System.out.println("user:");
        // for user
        List<Assignment> assignmentList = assignmentMapper.getVisibleAssignmentList();
        return  assignmentList;
    }



    @CrossOrigin
    @GetMapping("/user/selectAssignmentById")
    public Assignment selectAssignment(HttpServletRequest request, String id){
        int assignment_id = Integer.parseInt(id);
        // check something
        System.out.println("Authority:" + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
//        System.out.println("Credentials:" +  SecurityContextHolder.getContext().getAuthentication().getCredentials());
//        System.out.println("Credentials:" +  SecurityContextHolder.getContext().getAuthentication().getCredentials());
//        System.out.println("Details:" +  SecurityContextHolder.getContext().getAuthentication().getDetails());
        Collection<? extends GrantedAuthority> authorities =  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("view_all_assignment")) {
                System.out.println("auth" + authority.getAuthority());
                return  assignmentMapper.selectAssignmentById(assignment_id);
            }
        }
        Assignment assignment = assignmentMapper.selectAssignmentById(assignment_id);
//        List<Map> assignment = assignmentMapper.selectAssignmentById(assignment_id);
//        System.out.println(assignment);

        return  assignment;
    }






    @CrossOrigin
    @GetMapping("/admin/addAssignment")
//    pass the milisecond from 1970 start_date end_date should be long
    public int addAssignment(String id, String name, String start_date, String end_date, String descrition){
        int is_visible = 1;
        Date date = new Date();
        int assignment_id = Integer.parseInt(id);
        long sec =  date.getTime();
        Timestamp create_time = new Timestamp(sec);
        long start_sec = Long.parseLong(start_date);
        long end_sec = Long.parseLong(end_date);
        Timestamp start_time = new Timestamp(start_sec);
        Timestamp end_time = new Timestamp(end_sec);
        Assignment new_assignment = new Assignment(assignment_id, name, create_time, start_time, end_time, descrition, is_visible);
        int ret = assignmentMapper.addAssignment(new_assignment);
        return ret;
    }

    //todo
    @CrossOrigin
    @GetMapping("/admin/updateAssignment")
//    pass the milisecond from 1970
    public int updateAssignment(String id, String name, String start_date, String end_date, String description){
        int is_visible = 1;
        int assignment_id = Integer.parseInt(id);
        Assignment cur_assignment = assignmentMapper.selectAssignmentById(assignment_id);
        Timestamp create_time = cur_assignment.getAssignment_create_time();
        long start_sec = Long.parseLong(start_date);
        long end_sec = Long.parseLong(end_date);
        Timestamp start_time = new Timestamp(start_sec);
        Timestamp end_time = new Timestamp(end_sec);
        Assignment new_assignment = new Assignment(assignment_id, name, cur_assignment.getAssignment_create_time(), start_time, end_time, description, is_visible);
        int ret = assignmentMapper.updateAssignment(new_assignment);
        return ret;
    }


    @CrossOrigin
    @GetMapping("/queryQuestionsByAssignmentID")
//    pass the id return assignment with associate questions
    public List<Question> queryQuestionsByAssignment(String id){
        int assignment_id = Integer.parseInt(id);
        Assignment assignment = assignmentMapper.queryQuestionsByAssignment(assignment_id);
        return assignment.getQuestions();
    }


//    @CrossOrigin
//    @GetMapping("/queryAssignmentsWithSid")
////    pass the id return assignment with associate questions
//    public List<Assignment> queryQuestionsByAssignment(String sid, String as_id){
//        int student_id;
//        int assignment_id;
//        try{
//            student_id = Integer.parseInt(sid);
//            assignment_id = Integer.parseInt(as_id);
//        } catch (Exception e){
//            return null;
//        }
//
//        Assignment assignment = assignmentMapper.queryQuestionsByAssignment(assignment_id);
//        return assignment.getQuestions();
//    }



}
