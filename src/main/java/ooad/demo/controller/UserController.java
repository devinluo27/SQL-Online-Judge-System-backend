package ooad.demo.controller;

import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.UserDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

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
    @GetMapping(value = "/react/login")
    // password not null, sid must be int
    public int login(int sid, String password) {
        UserDB user = userMapper.selectUserDBBySid(sid);
        String pwd = userMapper.getPwd(sid);
        System.out.println(sid);
        System.out.println(user);
        System.out.println(pwd);
        if (pwd == null){
            return 0; // no such a sid
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
    public int addUser(int sid, String user_name, String password) {
        UserDB user_by_sid = userMapper.selectUserDBBySid(sid);
        UserDB user_by_name = userMapper.selectUserDBByName(user_name);

        if(user_by_sid != null){
            return -1; //user_sid already exists
        }
        else if( user_by_name != null){
            return -2; //user_name already exists
        }
        UserDB new_user = new UserDB(0, sid, user_name, password);
        userMapper.addUserDB(new_user);
        return 1;
    }

}
