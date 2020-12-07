package ooad.demo.mapper;

import ooad.demo.pojo.UserDB;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface UserMapper{
    List<UserDB> queryUserDBList();

    UserDB selectUserDBBySidAllInfo(int sid);

    UserDB selectUserDBByName(String user_name);

    int setLastLoginTime(Integer sid);

    int addUserDB(UserDB userDB);

    int resetUserDBName(int sid, String user_name);

    int resetUserDBPassword(int sid, String pwd);

    int deleteUserDB(int sid);

    String getPwd(int sid);
}
