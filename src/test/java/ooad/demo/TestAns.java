package ooad.demo;

import ooad.demo.Service.JudgeService;
import ooad.demo.controller.RecordController;
import ooad.demo.controller.SysPermissionController;
import ooad.demo.controller.UserController;
import ooad.demo.judge.Remote;
import ooad.demo.mapper.*;
import ooad.demo.pojo.UserDB;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ooad.demo.controller.SysPermissionController;
import ooad.demo.mapper.SysPermissionMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.UserDB;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAsync
public class TestAns {
    @Autowired
    UserMapper userMapper;

    @Autowired
    SysPermissionMapper sysPermissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysPermissionController sysPermissionController;

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
    RecordController recordController;

    @Autowired
    JudgeService judgeService;

    @Autowired
    Remote remote;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .build();
    }

    @org.junit.Test
    public void login() throws Exception {
        // given
        mockMvc
                // do
                .perform(
                        post("/login").with(csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "11813221")
                                .param("password", "666666")
//                        post("/user/addRecord").with(csrf())

                )
                // then
                .andExpect(status().isOk());
    }

    @org.junit.Test
    public void addRecordTest() throws Exception {
        // given
//        mockMvc
//                // do
//                .perform(
//                        post("/login").with(csrf())
//                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                                .param("username", "11813221")
//                                .param("password", "666666"))
////                        post("/user/addRecord").with(csrf())
//
//                // then
//                .andExpect(status().isOk())
//                .andExpect(cookie());

        mockMvc
                // do
                .perform(
                        post("/user/addRecord").with(csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("question_id", "100")
                                .param("code", "666666")
                                .param("type", "postresql").cookie())
//                        post("/user/addRecord").with(csrf())
                // then
                .andExpect(status().isOk());
    }




}
