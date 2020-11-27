package ooad.demo.mapper;

import ooad.demo.pojo.UserFile;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserFileMapper {
    //根据登陆用户id 获取用户的文件列表信息
    List<UserFile> findByUserId(Integer id);

    //保存用户的文件信息
    void save(UserFile userFile);

    UserFile findById(Integer id);

    void update(UserFile userFile);

    void delete(Integer id);

    List<UserFile> getAllFile();
}
