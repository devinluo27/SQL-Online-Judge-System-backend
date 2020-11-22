package ooad.demo.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
public class CookiesController {
    @GetMapping("/read")
    public String read(@CookieValue String username){
        return username;
    }
    @GetMapping("/write")
    public String write(HttpServletResponse response){
        Cookie cookie = new Cookie("username", "uservalue3");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        cookie.setMaxAge(100);
        response.addCookie(cookie);

        return "OK";
    }
}
