package ooad.demo.Service;

import ooad.demo.pojo.UserFile;

import java.util.List;

public interface UserFileService {
    List<UserFile> findByUserId(Integer id);
    List<UserFile> getAllFile();


    void save(UserFile userFile);

    UserFile findById(Integer id);

    void update(UserFile userFile);

    void delete(Integer id);
}
