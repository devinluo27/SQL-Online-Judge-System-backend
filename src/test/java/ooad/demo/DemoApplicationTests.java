package ooad.demo;

import ooad.demo.controller.UserController;
import ooad.demo.mapper.RecordMapper;
import ooad.demo.mapper.VerifyCodeMapper;
import ooad.demo.pojo.VerifyCode;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    DataSource dataSource;

    @Autowired
    RecordMapper recordMapper;

    @Autowired
    VerifyCodeMapper verifyCodeMapper;

    @Autowired
    JavaMailSenderImpl mailSender;

    @Test
    void  vCodeTest(){
        verifyCodeMapper.insertVerifyCode(123,123321);
//        VerifyCode v = verifyCodeMapper.getVerifyCode(1223);
//        System.out.println(v.getCid());
//        System.out.println(v.getV_code());
//        System.out.println(v.getExpired_time());
    }

    @Test
    void  vCodeTest1(){
        System.out.print(verifyCodeMapper.getVerifyCode(123));
    }

    @Autowired
    UserController userController;

    @Test
    void  vCodeTest2(){
        System.out.print(userController.resetPassword("123",123321,"123321"));
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

        //获得数据库连接
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void judge() {
        String standard = "select * from record";
        String code = "elect * from record";
        List<LinkedHashMap<String, Object>>  a;
        try {
            a = recordMapper.judge(standard, code);
        }
        catch (DataAccessException e){

        }
//        System.out.println(a.size() == 0);
    }

    @Test
    public void getPublicData(){
        String sql = "";
        List<LinkedHashMap<String, Object>> list = recordMapper.runSql(sql);
    }

}
