package ooad.demo;

import com.jcraft.jsch.JSchException;
import ooad.demo.Service.JudgeService;
import ooad.demo.Service.JudgeServiceImpl;
import ooad.demo.Service.UserFileService;
import ooad.demo.controller.RecordController;
import ooad.demo.controller.UserController;
import ooad.demo.judge.ManageDockersPool;
import ooad.demo.judge.Remote;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.mapper.VerifyCodeMapper;
import ooad.demo.pojo.UserDB;
import ooad.demo.pojo.VerifyCode;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ooad.demo.judge.Remote;

@SpringBootTest
@EnableAsync
class DemoApplicationTests {
    @Autowired
    DataSource dataSource;

    @Autowired
    RecordMapper recordMapper;

    @Autowired
    VerifyCodeMapper verifyCodeMapper;

    @Autowired
    UserController userController;

    @Autowired
    JavaMailSenderImpl mailSender;

    @Autowired
    UserMapper userMapper;
    @Autowired
    RecordController recordController;

    @Autowired
    JudgeService judgeService;

    @Autowired
    Remote remote;

    @Value("${test.num}")
    int a = 1;

    @Test
    void judgeTest() throws IOException, JSchException, InterruptedException {

//        System.out.println(judgeService.a());
        System.out.println("test: " + a);
//        String standard_ans = "select * from movies";
//        for (int i = 0 ; i  < 100; i++) {
//            String code = "select * from movies";
//            long start = System.currentTimeMillis();
//            int sid = i % 3 + 1;
//            if (i % 5== 0){
//                code = "hello world!";
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//        Thread.sleep(10000000);

    }

//    @Test
//    void judgeTest_docker_1() throws IOException, JSchException, InterruptedException {
//        for (int i = 0; i < 100; i++){
//            recordController.addRecord(1, "select * from movies", "postgresql");
//            System.out.println("submit one");
//        }
//        System.out.println("==================Finished submission ===================");
//        System.out.println("==================Finished submission ===================");
//        long now = System.currentTimeMillis();
//        System.out.println(now);
//        while(System.currentTimeMillis() - now < 100000){}
//    }


    @Test
    void judgeTest_docker_2() throws IOException, JSchException, InterruptedException {
//        ManageDockersPool.getInstance().getDockersPoolHashMap().get("1-")
        String[] strings = new String[1];
        strings[0] = "echo haha";
        ArrayList<Remote.Log> r = remote.EXEC_CMD(strings);
        System.out.println(r.get(0).getOUT());
    }



        @Test
    void  vCodeTest(){
//        verifyCodeMapper.insertVerifyCode(123,123321);
//        VerifyCode v = verifyCodeMapper.getVerifyCode(1223);
//        System.out.println(v.getCid());
//        System.out.println(v.getV_code());
//        System.out.println(v.getExpired_time());
    }

    @Test
    void  vCodeTest1(){
        System.out.print(verifyCodeMapper.getVerifyCode(123));
    }



    @Test
    public void contextLoads1() throws SQLException {
        System.out.println("????????????");
        System.out.println("dataSource.getClass()??????***"+dataSource.getClass());

        Connection connection = dataSource.getConnection();
        System.out.println("connection??????***"+connection);
        connection.close();

    }
    @Test
    void  vCodeTest2(){
//        System.out.print(userController.resetPassword(123,123321,"123321"));
    }



    @Test
    void mailTest(){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("CS 307 Database Account Verification");
        mailMessage.setText("asd");
        mailMessage.setTo("11813221@mail.sustech.edu.cn");
        mailMessage.setFrom("945517787@qq.com");

        mailSender.send(mailMessage);
    }

    @Test
    void contextLoads() throws SQLException {
        System.out.print(dataSource.getClass());

        //?????????????????????
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void getUser(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        List<UserDB> userDBList =  userController.queryUserDBList();
        for (int i = 0; i < userDBList.size(); i++){
             String encodePassword = passwordEncoder.encode(userDBList.get(i).getUser_password());
             UserDB user = userDBList.get(i);
             userMapper.resetUserDBPassword(user.getSid(), encodePassword);
        }
    }
    

    @Autowired
    UserFileService userFileService;

    @Test
    public void fileDelete(){
        userFileService.delete(1);
    }

    @Test
    public void copyToRemote() throws IOException {

//        userFileService.copyToRemoteHost(1);
    }

    @Test
    public void copyToRemote1() throws Exception {
//        remote.uploadFile(9);
    }

}
