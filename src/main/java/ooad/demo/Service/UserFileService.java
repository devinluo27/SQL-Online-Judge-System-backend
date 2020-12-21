package ooad.demo.Service;

import ooad.demo.pojo.UserFile;

import java.io.IOException;
import java.util.List;

public interface UserFileService {
    List<UserFile> findByUserId(Integer id);

    List<UserFile> getAllFileInfo();


    void save(UserFile userFile);

    UserFile findById(Integer id);

    void update(UserFile userFile);

    void delete(Integer id);

    boolean copyToRemoteHost(Integer file_id) throws IOException;

    Integer setFileIsRemoteStatus(Integer file_id, Boolean status);

}
