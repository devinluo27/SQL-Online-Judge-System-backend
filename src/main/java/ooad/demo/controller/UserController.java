package ooad.demo.controller;

import cn.shuibo.annotation.Encrypt;
import com.alibaba.fastjson.JSON;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
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

import javax.persistence.criteria.CriteriaBuilder;
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


    @CrossOrigin
    @GetMapping("/admin/queryUserList")
    public List<UserDB> queryUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        return  userDBList;
    }

    @CrossOrigin
//    @GetMapping(value = "/admin/findUserBySid")
    public UserDB findUserDBBySid(int sid){
        UserDB user_by_sid = userMapper.selectUserDBBySidAllInfo(sid);
        return user_by_sid; //user_sid already exists
    }


    @CrossOrigin
//    @GetMapping(value = "/user/addUser")
    // password not null len >=6, sid must be int
    public int addUser(int sid, String user_name, String password, String authority) {
        UserDB user_by_sid = userMapper.selectUserDBBySidAllInfo(sid);
        UserDB user_by_name = userMapper.selectUserDBByName(user_name);

        if(user_by_sid != null){
            return -1; //user_sid already exists
        }
        else if( user_by_name != null){
            return -2; //user_name already exists
        }
        UserDB new_user = new UserDB(sid, user_name, new BCryptPasswordEncoder().encode(password), authority);
        userMapper.addUserDB(new_user);
        return 1;
    }

    @Encrypt
    @GetMapping(value = "/username")
    @ResponseBody
    public String currentUserNameSimple(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null)
            return "Please login!";
        return principal.getName();
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
     *
     */
    @CrossOrigin
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

}
