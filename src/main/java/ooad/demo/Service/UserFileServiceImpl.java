package ooad.demo.Service;


import ooad.demo.mapper.UserFileMapper;
import ooad.demo.pojo.UserFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class UserFileServiceImpl implements UserFileService {

    @Autowired
    UserFileMapper userFileMapper;

    @Override
    public List<UserFile> findByUserId(Integer id) {
        return userFileMapper.findByUserId(id);
    }

    @Override
    public void save(UserFile userFile) {
        //是否是图片解决方案：当类型中含有image时 说明当前类型一定为图片类型
//        userFile.setIsImg()
        String isImg = userFile.getType().startsWith("image") ? "是" : "否";
        userFile.setIsImg(isImg);
        userFile.setDowncounts(0);
        userFile.setUploadTime(new Timestamp(System.currentTimeMillis()));
        userFileMapper.save(userFile);
    }

    @Override
    public UserFile findById(Integer id) {
        return userFileMapper.findById(id);
    }

    @Override
    public void update(UserFile userFile) {
        userFileMapper.update(userFile);
    }

    @Override
    public void delete(Integer id) {
        userFileMapper.delete(id);
    }

    public List<UserFile> getAllFile(){
        return userFileMapper.getAllFile();
    }

}
