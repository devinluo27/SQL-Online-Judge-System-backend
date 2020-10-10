package ooad.demo.controller;

import ooad.demo.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {
    @Autowired
    private RecordMapper recordMapper;

}
