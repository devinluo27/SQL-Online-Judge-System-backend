package ooad.demo.Service;

import lombok.extern.slf4j.Slf4j;
import ooad.demo.mapper.UserMapper;
import ooad.demo.mapper.VerifyCodeMapper;
import ooad.demo.pojo.UserDB;
import ooad.demo.pojo.VerifyCode;
import ooad.demo.utils.AccessLimit;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

@Slf4j
@Service
public class EmailService {
    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private UserMapper userMapper;

    /***
     *
     * @param
     * @return mail sends succeed: 1
     * need to wait: 0
     */
    @AccessLimit(seconds = 10, maxCount = 1, needLogin = false)
    @ResponseBody
//    @Async
    public void sendVerifyCodeNotGivenAddress(Integer sid){

        UserDB userdb = userMapper.selectUserDBBySidBasicInfo(sid);
        // get latest verifyCode
        VerifyCode v = verifyCodeMapper.getVerifyCode(sid);

        if (v == null || System.currentTimeMillis() - v.getCreated_time().getTime() > 30000){
            log.info("Generate V code!");
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("CS 307 Database Account Verification");
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            String receiver_address = userdb.getEmail_addr();

            verifyCodeMapper.insertVerifyCode(sid, code, receiver_address);
            String mail_response = "Your Verification code is " + code + ".\nExpired in 5 minutes.";
            mailMessage.setText(mail_response);

            // TODO: To be improved

            mailMessage.setTo(receiver_address);
            mailMessage.setFrom("945517787@qq.com");
            mailSender.send(mailMessage);
        }

    }

    /***
     *
     * @param
     * @return mail sends succeed: 1
     * need to wait: 0
     */
    @AccessLimit(seconds = 10, maxCount = 1, needLogin = false)
    @ResponseBody
//    @Async
    public void sendVerifyCodeWithAddress(Integer sid, String receiver_address) throws IOException {

        UserDB userdb = userMapper.selectUserDBBySidBasicInfo(sid);
        if (userdb == null){
            log.warn("User " + sid + " is not registered!");
            return;
        }
        // get latest verifyCode
        VerifyCode v = verifyCodeMapper.getVerifyCode(sid);

        if (v == null || System.currentTimeMillis() - v.getCreated_time().getTime() > 30000){
            log.info("Generate V code!");
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("CS 307 Database Account Verification");
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            verifyCodeMapper.insertVerifyCode(sid, code, receiver_address);
            String mail_response = "Your Verification code is " + code + ".\nExpired in 5 minutes.";
            mailMessage.setText(mail_response);
            // TODO: To be improved
            mailMessage.setTo(receiver_address);
            mailMessage.setFrom("945517787@qq.com");
            mailSender.send(mailMessage);
        }

    }
}
