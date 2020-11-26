package ooad.demo.controller;

import ooad.demo.mapper.UserMapper;
import ooad.demo.mapper.VerifyCodeMapper;
import ooad.demo.pojo.UserDB;
import ooad.demo.pojo.VerifyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

@RestController
public class UserController {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @CrossOrigin
//    @GetMapping("/admin/queryUserList")
    public List<UserDB> queryUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        for (UserDB  user: userDBList){
            System.out.println(user);
        }
        return  userDBList;
    }

    @CrossOrigin
//    @GetMapping(value = "/admin/findUserBySid")
    public UserDB findUserDBBySid(int sid){
        UserDB user_by_sid = userMapper.selectUserDBBySid(sid);
        return user_by_sid; //user_sid already exists
    }

    /***
     *
     * @param sid
     * @param pwd
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/user/login")
    // password not null, sid must be int
    public int login(String sid, String pwd) {
        int id;
        try {
            id = Integer.parseInt(sid);
        } catch (Exception e){
            return -2;
        }
        UserDB user = userMapper.selectUserDBBySid(id);
        String correct_pwd = userMapper.getPwd(id);

        if (pwd == null){
            return -3; // no such a sid
        }
        else if (correct_pwd.equals(pwd)){
            return 1; // login success
        }
        // incorrect password
        return -1;
    }

    @CrossOrigin
//    @GetMapping(value = "/admin/addUser")
    // password not null len >=6, sid must be int
    public int addUser(int sid, String user_name, String password, String authority) {
        UserDB user_by_sid = userMapper.selectUserDBBySid(sid);
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
    public int resetPassword(String sid, int v_code, String pwd){
        int id = Integer.parseInt(sid);
        VerifyCode v = verifyCodeMapper.getVerifyCode(id);

        if (findUserDBBySid(id) == null){
            return -1;
        }
        if (v == null || v_code != v.getV_code()){
            return -2;
        }
        if(System.currentTimeMillis() - v.getCreated_time().getTime() > 3e5){
            return -3;
        }
//        userMapper.resetUserDBPassword(id, passwordEncoder.encode(pwd));
        userMapper.resetUserDBPassword(id, pwd);
        return 1;
    }


    @Autowired
    JavaMailSenderImpl mailSender;

    /***
     *
     * @param sid
     * @return mail sends succeed: 1
     * need to wait: 0
     */
    @GetMapping(value = "/user/sendVerifyCode")
    @Async
    public int sendVerifyCode(String sid){
        int id = Integer.parseInt(sid);
        VerifyCode v = verifyCodeMapper.getVerifyCode(id);
        if (v == null || System.currentTimeMillis() - v.getCreated_time().getTime() > 30000){
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("CS 307 Database Account Verification");
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            verifyCodeMapper.insertVerifyCode(id, code);
            String response = "Your Verification code is " + code + ".\n Expired in 5 minutes.";
            mailMessage.setText(response);
            mailMessage.setTo( sid + "@mail.sustech.edu.cn");
            mailMessage.setFrom("945517787@qq.com");
            mailSender.send(mailMessage);
            return 1;
        }
        return 0;
    }

//    public int reset(int sid, String password){
//        UserMapper.
//    }

}
