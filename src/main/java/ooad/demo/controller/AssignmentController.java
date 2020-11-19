package ooad.demo.controller;


import ooad.demo.mapper.AssignmentMapper;
import ooad.demo.pojo.Assignment;
import ooad.demo.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AssignmentController {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @CrossOrigin
    @GetMapping("/queryAssigmentList")
    public List<Assignment> queryAssigmentList(){
        List<Assignment> assignmentList = assignmentMapper.queryAssignmentList();
        return  assignmentList;
    }

    @CrossOrigin
    @GetMapping("/selectAssignmentById")
    public Assignment selectAssignment(String id){
        int assignment_id = Integer.parseInt(id);
        Assignment assignment = assignmentMapper.selectAssignmentById(assignment_id);
//        List<Map> assignment = assignmentMapper.selectAssignmentById(assignment_id);
        System.out.println(assignment);

        return  assignment;
    }

    @CrossOrigin
    @GetMapping("/addAssignment")
//    pass the milisecond from 1970 start_date end_date should be long
    public int addAssignment(String id, String name, String start_date, String end_date, String descrition){
        Date date = new Date();
        int assignment_id = Integer.parseInt(id);
        long sec =  date.getTime();
        Timestamp create_time = new Timestamp(sec);
        long start_sec = Long.parseLong(start_date);
        long end_sec = Long.parseLong(end_date);
        Timestamp start_time = new Timestamp(start_sec);
        Timestamp end_time = new Timestamp(end_sec);
        Assignment new_assignment = new Assignment(assignment_id, name, create_time, start_time, end_time, descrition);
        int ret = assignmentMapper.addAssignment(new_assignment);
        return ret;
    }

    //todo
    @CrossOrigin
    @GetMapping("/updateAssignment")
//    pass the milisecond from 1970
    public int updateAssignment(String id, String name, String start_date, String end_date, String description){
        int assignment_id = Integer.parseInt(id);
        Assignment cur_assignment = assignmentMapper.selectAssignmentById(assignment_id);
        Timestamp create_time = cur_assignment.getAssignment_create_time();
        long start_sec = Long.parseLong(start_date);
        long end_sec = Long.parseLong(end_date);
        Timestamp start_time = new Timestamp(start_sec);
        Timestamp end_time = new Timestamp(end_sec);
        Assignment new_assignment = new Assignment(assignment_id, name, create_time, start_time, end_time, description);
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
