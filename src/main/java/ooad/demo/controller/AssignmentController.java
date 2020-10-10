package ooad.demo.controller;


import ooad.demo.mapper.AssignmentMapper;
import ooad.demo.pojo.Assignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


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
    public Assignment selectAssignment(String assignment_id){
        int id = Integer.parseInt(assignment_id);
        Assignment assignment = assignmentMapper.selectAssignmentById(id);
        return  assignment;
    }

    @CrossOrigin
    @GetMapping("/addAssignment")
//    pass the milisecond from 1970
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


}
