package ooad.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequestMapping("/hello")
public class Hello_controller {

    @GetMapping("/hello")
    @CrossOrigin
    @ResponseBody
    public String hello(String id, String password){
        return "hello2";

    }
}
