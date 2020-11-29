package ooad.demo.controller;

import ooad.demo.mapper.SysPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SysPermissionController {

    @Autowired
    SysPermissionMapper sysPermissionMapper;

}
