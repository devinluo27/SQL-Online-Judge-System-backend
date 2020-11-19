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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/queryUserList")
    public List<UserDB> queryUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        for (UserDB  user: userDBList){
            System.out.println(user);
        }
        return  userDBList;
    }

    @CrossOrigin
    @GetMapping(value = "/react/findUserBySid")
    public UserDB findUserDBBySid(int sid){
        UserDB user_by_sid = userMapper.selectUserDBBySid(sid);
        return user_by_sid; //user_sid already exists
    }

    @CrossOrigin
    @GetMapping(value = "/react/login")
    // password not null, sid must be int
    public int login(String id, String password) {
        int sid;
        try {
            sid = Integer.parseInt(id);
        } catch (Exception e){
            return -2;
        }
        UserDB user = userMapper.selectUserDBBySid(sid);
        String pwd = userMapper.getPwd(sid);

        if (pwd == null){
            return -3; // no such a sid
        }
        else if (pwd.equals(password)){
            return 1; // login success
        }
        // incorrect password
        return -1;
    }

    @CrossOrigin
    @GetMapping(value = "/react/add")
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

    @CrossOrigin
    @GetMapping(value = "/react/resetPwd")
    public int resetPassword(String sid, String pwd){
        int id = Integer.parseInt(sid);
        if (findUserDBBySid(id) == null){
            return -1; // not exist
        }
        userMapper.resetUserDBPassword(id, pwd);
        return 1;
    }

    @CrossOrigin
    @GetMapping(value = "/user/resetPwd")
    public int resetPassword(String sid, int v_code, String pwd){
        int id = Integer.parseInt(sid);
        VerifyCode v = verifyCodeMapper.getVerifyCode(id);

        // 不存在该用户
        if (findUserDBBySid(id) == null){
            return -1; // not exist
        }

        // no verify code == 验证码错误
        if (v == null){
            return -2;
        }
        // 验证码过期
        if(System.currentTimeMillis() - v.getExpired_time().getTime() > 3e5){
            return -3;
        }

        userMapper.resetUserDBPassword(id, passwordEncoder.encode(pwd));
        return 1;
    }



    @Autowired
    JavaMailSenderImpl mailSender;

    @GetMapping(value = "/user/sendVerifyCode")
    @ResponseBody
    @Async
    public int sendVerifyCode(String sid){
        int id = Integer.parseInt(sid);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("CS 307 Database Account Verification");
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        // TODO
        Timestamp sendTime = new Timestamp(System.currentTimeMillis());

        String response = "Your Verification code is " + code + ".\n Expired in 5 minutes.";
        mailMessage.setText(response);
        mailMessage.setTo( sid + "@mail.sustech.edu.cn");
        mailMessage.setFrom("945517787@qq.com");
//        mailSender.send(mailMessage);
        return 1;
    }


}
