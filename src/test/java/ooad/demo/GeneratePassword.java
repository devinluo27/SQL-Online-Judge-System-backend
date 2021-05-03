package ooad.demo;

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
        int sid = 11911716;
        String pwd = "11911716";
        userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
    }



    @Test
    public void addUserWithOnlyRoleStudent() {
        ArrayList<UserDB> list = new ArrayList<>();
//        list.add(new UserDB(11812520, "张淘月", passwordEncoder.encode("taoyue2520")));
//        list.add(new UserDB(11912714, "任艺伟", passwordEncoder.encode("yiwei2714")));
//        list.add(new UserDB(11712009, "卢弘毅", passwordEncoder.encode("hongyi2009")));
//        list.add(new UserDB(11812704, "王硕", passwordEncoder.encode("wangshuo2704")));
//        list.add(new UserDB(11712310, "王子勤", passwordEncoder.encode("ziqin2310")));
//        list.add(new UserDB(11712019, "余添诚", passwordEncoder.encode("tiancheng2019")));
//        list.add(new UserDB(11812720, "朱祖耀", passwordEncoder.encode("zuyao2720")));
//        list.add(new UserDB(30001219, "朱悦铭", passwordEncoder.encode("yueming1219")));
//        list.add(new UserDB(30009746, "王维语", passwordEncoder.encode("weiyu9746")));
//        list.add(new UserDB(11811031, "何宜芮", passwordEncoder.encode("yirui1031")));
//        list.add(new UserDB(11811237, "汤怡宁", passwordEncoder.encode("yining1237")));
//        list.add(new UserDB(11510218, "杨晓苏", passwordEncoder.encode("xiaosu0218")));

        for (UserDB u: list){
            userMapper.addUserDB(u);
            sysPermissionMapper.addNewRoleToUser(u.getSid(), "user");
        }

    }

    @Test
    public void addUserWith3Roles() {
        ArrayList<UserDB> userDBs = new ArrayList<>();
        int sid = 1181;
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
        ArrayList<UserDB> userDBs = new ArrayList<>();
        UserDB u1 = new UserDB(11812613,"香佳宏",
                passwordEncoder.encode("DBOJ666!"));
        userDBs.add(u1);
        for (int i = 0 ; i< userDBs.size(); i++){
            UserDB temp = userDBs.get(i);
            userMapper.addUserDB(temp);
            sysPermissionMapper.addNewRoleToUser(temp.getSid(), "user");
            sysPermissionMapper.addNewRoleToUser(temp.getSid(), "TA");
        }
    }

    /***
     * change password to Encode one
     */
    @Test
    public void generateNewPwd1() {
        List<UserDB> list = userMapper.queryAllUserDBList();
        for (int i = 0; i < list.size(); i++) {
            UserDB stud = list.get(i);
            if (stud.getLab_num() != null && (stud.getLab_num() == 4 || stud.getLab_num() == 5)){
                int sid = list.get(i).getSid();
                String pwd = list.get(i).getUser_password();
                System.out.println(sid + ", " + pwd);
            userMapper.resetUserDBPassword(sid, passwordEncoder.encode(pwd));
            }
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
