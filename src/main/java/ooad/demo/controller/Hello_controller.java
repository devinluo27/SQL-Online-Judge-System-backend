package ooad.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/hello")
public class Hello_controller {

    @GetMapping("/hello")
    @CrossOrigin
    public String hello(String id, String password){

        return "hello2";

    }
}
