package ooad.demo.mapper;

import ooad.demo.pojo.UserDB;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface UserMapper{
    List<UserDB> queryUserDBList();

    public static final int age = 18;

    UserDB selectUserDBBySid(int sid);
    UserDB selectUserDBByName(String user_name);

    int addUserDB(UserDB userDB);

    int updateUserDB(UserDB userDB);

    int deleteUserDB(int sid);

    String getPwd(int sid);
}
