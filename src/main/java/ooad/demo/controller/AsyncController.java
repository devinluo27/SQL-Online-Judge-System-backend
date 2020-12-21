package ooad.demo.controller;

import ooad.demo.Service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncController {
    @Autowired
    AsyncService asyncService;

    @RequestMapping("/async")
    public String as(){
        asyncService.hello();
        return "ok";
    }

    @RequestMapping("/hi")
    public String hi(){
        return "hi";
    }
}
