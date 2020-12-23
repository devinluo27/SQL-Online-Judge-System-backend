package ooad.demo.controller;

import cn.shuibo.annotation.Encrypt;
import com.alibaba.fastjson.JSON;
import ooad.demo.utils.AccessLimit;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import ooad.demo.mapper.UserMapper;
import ooad.demo.mapper.VerifyCodeMapper;
import ooad.demo.pojo.UserDB;
import ooad.demo.pojo.VerifyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Random;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSenderImpl mailSender;

    // TESTING
    @GetMapping(value = "/username")
    @ResponseBody
    public String currentUserNameSimple(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null)
            return "Please login!";
        return principal.getName();
    }

    @GetMapping("/admin/queryUserList")
    public List<UserDB> queryUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        return  userDBList;
    }

    // TODO: NEW API
    @GetMapping("/admin/queryALLUserList")
    public List<UserDB> queryAllUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        return  userDBList;
    }

    @GetMapping(value = "/admin/findUserBySid")
    public UserDB findUserDBBySid(int sid){
        return userMapper.selectUserDBBySidBasicInfo(sid); //user_sid already exists
    }

    @AccessLimit(maxCount = 3,seconds = 30, needLogin = false)
    @PostMapping(value = "/user/addUser")
    public void addUser(
            @RequestParam(value = "sid") int sid,
            @RequestParam(value = "sid") String user_name,
            @RequestParam(value = "sid") String password,
            HttpServletResponse response) {
        UserDB user_by_sid = userMapper.selectUserDBBySidAllInfo(sid);
        // password not null len >=6, sid must be int
        if (password == null || password.length() < 6){
            ResultTool.writeResponseFail(response, ResultCode.COMMON_FAIL);
            return;
        }
        if(user_by_sid != null){
            ResultTool.writeResponseFail(response, ResultCode.USER_ALREADY_EXISTS);
            return; //user_sid already exists
        }
        UserDB new_user = new UserDB(sid, user_name, new BCryptPasswordEncoder().encode(password));
        userMapper.addUserDB(new_user);
        ResultTool.writeResponseSuccess(response);
    }

    /***
     *
     * @param sid
     * @param v_code
     * @param pwd
     * @return
     * -1: user doesn't exist
     * -2: v_code error
     * -3: v_code expired
     */
    @GetMapping(value = "/user/resetPwd")
    public int resetPassword(
            @RequestParam(value = "sid") Integer sid,
            @RequestParam(value = "v_code") int v_code,
            @RequestParam(value = "pwd") String pwd){
        VerifyCode v = verifyCodeMapper.getVerifyCode(sid);
        if (findUserDBBySid(sid) == null){
            return -1;
        }
        if (v == null || v_code != v.getV_code()){
            return -2;
        }
        if(System.currentTimeMillis() - v.getCreated_time().getTime() > 3e5){
            return -3;
        }
        userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
        return 1;
    }

    /***
     *
     * @param
     * @return mail sends succeed: 1
     * need to wait: 0
     */
    @GetMapping(value = "/user/sendVerifyCode")
    @Async
    public void sendVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        if (request.getUserPrincipal() == null){
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        VerifyCode v = verifyCodeMapper.getVerifyCode(sid);
        if (v == null || System.currentTimeMillis() - v.getCreated_time().getTime() > 30000){
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("CS 307 Database Account Verification");
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            verifyCodeMapper.insertVerifyCode(sid, code);
            String mail_response = "Your Verification code is " + code + ".\n Expired in 5 minutes.";
            mailMessage.setText(mail_response);
            mailMessage.setTo( sid + "@mail.sustech.edu.cn");
            mailMessage.setFrom("945517787@qq.com");
            mailSender.send(mailMessage);
            JsonResult result = ResultTool.success();
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        JsonResult result = ResultTool.fail();
        response.getWriter().write(JSON.toJSONString(result));
    }

    // TODO: NEW URL
    @GetMapping("/user/loginCountToday")
    public void loginCountToday(){

    }

}
