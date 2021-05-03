package ooad.demo.controller;

import lombok.extern.slf4j.Slf4j;
import ooad.demo.Service.EmailService;
import ooad.demo.utils.AccessLimit;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Random;

// TODO: Something wrong when using @Async cannot get cookie!!!


@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSenderImpl mailSender;

    @Autowired
    private EmailService emailService;

    // 获得当前user的用户名 8
    @GetMapping(value = "/username")
    @ResponseBody
    public String currentUserNameSimple(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null)
            return "Please login!";
        return principal.getName();
    }

    // 35
    @GetMapping("/admin/queryUserList")
    public List<UserDB> queryUserDBList(){
        List<UserDB> userDBList = userMapper.queryUserDBList();
        return  userDBList;
    }

    // 40
    @GetMapping(value = "/admin/findUserBySid")
    public UserDB findUserDBBySid(int sid){
        return userMapper.selectUserDBBySidBasicInfo(sid); //user_sid already exists
    }

    // TODO:
    @AccessLimit(maxCount = 3,seconds = 3, needLogin = false)
    @PostMapping(value = "/admin/addUser")
    public void addUser(
            @RequestParam(value = "sid") int sid,
            @RequestParam(value = "user_name") String user_name,
            @RequestParam(value = "pwd") String password,
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
        UserDB new_user = new UserDB(sid, user_name, passwordEncoder.encode(password));
        userMapper.addUserDB(new_user);
        ResultTool.writeResponseSuccess(response);
    }

    // TODO: New API Request Path 112 Permission 100 Owner: null
    @AccessLimit(maxCount = 3,seconds = 3, needLogin = true)
    @GetMapping("/root/admin/resetOtherPwd")
    public void resetOtherPwd(@RequestParam(value = "sid") Integer sid,
                              @RequestParam(value = "newPwd") String newPwd,
                              HttpServletResponse response){
         try{
             userMapper.resetUserDBPassword(sid, passwordEncoder.encode(newPwd));
             log.info("Admin is Changing Other Password: " + sid + ": changing password!");
             ResultTool.writeResponseSuccess(response);
         } catch (Exception e){
             log.error("Admin is Changing Other Password Fail! ", e);
             ResultTool.writeResponseFail(response);
         }
    }


    // TODO: New API
    /***
     * verify and set user email address
     * @param
     * @param request
     * @param response
     */
    @AccessLimit(maxCount = 3,seconds = 3, needLogin = true)
    @GetMapping("/user/getRegisterEmailVCode")
    public void getRegisterEmailVCode(@RequestParam(value = "email_addr") String email_addr,
                              HttpServletRequest request,
                              HttpServletResponse response){
        if(request.getUserPrincipal() == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN);
            return;
        }
        try{
            int sid = Integer.parseInt(request.getUserPrincipal().getName());
            emailService.sendVerifyCodeWithAddress(sid, email_addr);
            ResultTool.writeResponseSuccess(response);
        } catch (Exception e){
            log.error("User setting up email address Fails! ", e);
            ResultTool.writeResponseFail(response);
        }
    }

    // TODO: New API
    /***
     * verify and set user email address
     * @param
     * @param request
     * @param response
     */
    @AccessLimit(maxCount = 3,seconds = 3, needLogin = true)
    @GetMapping("/user/setEmailAddr")
    public void setEmailAddr(@RequestParam(value = "v_code") Integer v_code,
                                    HttpServletRequest request,
                                    HttpServletResponse response){
        if(request.getUserPrincipal() == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN);
            return;
        }
        try{
            int sid = Integer.parseInt(request.getUserPrincipal().getName());
            VerifyCode v = verifyCodeMapper.getVerifyCode(sid);

            if (v == null || v_code != v.getV_code() ||
                    System.currentTimeMillis() - v.getCreated_time().getTime() > 3e5){
                ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN);
                return;
            }
            userMapper.setUserDBEmailAddr(sid, v.getDest_addr());
            ResultTool.writeResponseSuccess(response);
        } catch (Exception e){
            log.error("User setting up email address Fails! ", e);
            ResultTool.writeResponseFail(response);
        }
    }



    // TODO: New API! No Permission Required
    /***
     *
     * @param
     * @param
     * @param pwd
     * @return
     * -1: user doesn't exist
     * -2: v_code error
     * -3: v_code expired
     */
    @GetMapping(value = "/user/resetPwdWithOutLogin")
    public void resetPwdWithOutLogin(
            @RequestParam(value = "sid") Integer sid,
            @RequestParam(value = "v_code") int v_code,
            @RequestParam(value = "pwd") String pwd,
            HttpServletResponse response){

        VerifyCode v = verifyCodeMapper.getVerifyCode(sid);
        if (findUserDBBySid(sid) == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_ACCOUNT_NOT_EXIST);
            return;
        }
        if(v == null || v_code != v.getV_code() ||
                System.currentTimeMillis() - v.getCreated_time().getTime() > 3e5 ){
            ResultTool.writeResponseFail(response);
            return;
        }
        userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
        ResultTool.writeResponseSuccess(response);
    }

    // TODO: New API! No Permission Required
    @AccessLimit(maxCount = 1, seconds = 10, needLogin = false)
    @GetMapping(value = "/user/getResetPwdVCode")
    public void getResetPwdVCode(@RequestParam(value = "sid") Integer sid,
                                 HttpServletRequest request,
                                 HttpServletResponse response){
        // check if this user exists
        if (findUserDBBySid(sid) == null){
            ResultTool.writeResponseFail(response, ResultCode.USER_ACCOUNT_NOT_EXIST);
            return;
        }
        try {
            emailService.sendVerifyCodeNotGivenAddress(sid);
            ResultTool.writeResponseSuccess(response);
        } catch (Exception e){
            log.error("/user/getResetPwdVCode", e);
            ResultTool.writeResponseFail(response);
        }
    }


    // TODO: New API
    /***
     * Reset Password after login (Normal Case)
     * @param pwd
     * @param request
     * @return
     */
    @GetMapping(value = "/user/resetPwd")
    public int resetPassword(
            @RequestParam(value = "pwd") String pwd,
            HttpServletRequest request){
        if(request.getUserPrincipal() == null){
//            ResultTool.writeResponseFail(response, ResultCode.USER_NOT_LOGIN);
            return -1;
        }
        int sid = Integer.parseInt(request.getUserPrincipal().getName());
        if (findUserDBBySid(sid) == null){
            return -1;
        }
        userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
        log.info(sid + ": changing password!");
        return 1;
    }


}
