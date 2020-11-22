package ooad.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RouterController {
    @RequestMapping({"/", "/index"})
    public String index(){
        return "bcc";
    }

    @RequestMapping({"/", "/index1"})
    public String index1(){
        return "index1";
    }
}
