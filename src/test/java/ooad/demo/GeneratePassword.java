package ooad.demo;

import ooad.demo.controller.SysPermissionController;
import ooad.demo.mapper.SysPermissionMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.UserDB;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;

@SpringBootTest
public class GeneratePassword {
    @Autowired
    UserMapper userMapper;

    @Autowired
    SysPermissionMapper sysPermissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysPermissionController sysPermissionController;
    @Test
    public void generateNewPwdRandom() {
        List<UserDB> list = userMapper.queryAllUserDBList();
        Random random = new Random();
        for (int i = 0; i < list.size(); i++){
            int sid = list.get(i).getSid();
            int pwd = random.nextInt(899999) + 100000;
            String str_pwd = String.valueOf(pwd);
            System.out.println(sid + ", " + str_pwd);
//            userMapper.resetUserDBPassword(sid, passwordEncoder.encode(str_pwd));

        }
    }

    @Test
    public void resetPwd() {
        int sid = 11813221;
        String pwd = "666666";
        userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
    }



    @Test
    public void addUserWithOnlyRoleStudent() {
        int sid = 11813221;
        String username = "罗云浩";
        String pwd = "666666";
        UserDB userDB = new UserDB(sid, username, passwordEncoder.encode(pwd));
        userMapper.addUserDB(userDB);
        sysPermissionMapper.addNewRoleToUser(sid, "user");
    }

    @Test
    public void addUserWith3Roles() {
        ArrayList<UserDB> userDBs = new ArrayList<>();
        int sid = 11813221;
        String username = "罗云浩";
        String pwd = "666666";
        for (int i = 0 ; i< userDBs.size(); i++){
            UserDB userDB = new UserDB(sid, username, passwordEncoder.encode(pwd));
            userMapper.addUserDB(userDB);
            sysPermissionMapper.addNewRoleToUser(sid, "user");
            sysPermissionMapper.addNewRoleToUser(sid, "TA");
            sysPermissionMapper.addNewRoleToUser(sid, "admin");
        }

    }

    @Test
    public void addUserWith2Roles() {

    }

    @Test
    public void generateNewPwd1() {
        List<UserDB> list = userMapper.queryAllUserDBList();
        for (int i = 0; i < list.size(); i++) {
            int sid = list.get(i).getSid();
            String pwd = list.get(i).getUser_password();
            System.out.println(sid + ", " + pwd);
            userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
        }
    }

    @Test
    public void giveRole3_student() {
        List<UserDB> list = userMapper.queryAllUserDBList();
        for (int i = 0; i < list.size(); i++) {
            sysPermissionMapper.addNewRoleToUser(list.get(i).getSid(), "user");
        }
    }
}
