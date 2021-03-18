package ooad.demo.Service;


import lombok.extern.slf4j.Slf4j;
import ooad.demo.judge.Remote;
import ooad.demo.mapper.UserFileMapper;
import ooad.demo.pojo.UserFile;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
@Transactional
public class UserFileServiceImpl implements UserFileService {

    @Autowired
    UserFileMapper userFileMapper;

    @Override
    public List<UserFile> findByUserId(Integer id) {
        return userFileMapper.findByUserId(id);
    }

    @Autowired
    Remote remote;

    @Value("${judge.remote.host}")
    private String remoteHost;

    @Value("${judge.remote.port}")
    private int remotePort;

    @Value("${judge.remote.username}")
    private String remoteUsername;

    @Value("${judge.remote.password}")
    private String remotePassword;

    @Value("${judge.remote.database-path}")
    private String remoteDatabasePath;


    @Override
    public void save(UserFile userFile) {
        // 是否是图片解决方案：当类型中含有image时 说明当前类型一定为图片类型
        String isImg = userFile.getFile_type().startsWith("image") ? "yes" : "no";
        userFile.setIs_img(isImg);
        userFile.setDown_counts(0);
        userFile.setUpload_time(new Timestamp(System.currentTimeMillis()));
        userFileMapper.save(userFile);
    }

    @Override
    public UserFile findById(Integer id) {
        return userFileMapper.findByFileId(id);
    }

    @Override
    public void update(UserFile userFile) {
        userFileMapper.updateDownCount(userFile);
    }

    @Override
    public void delete(Integer id) {
        userFileMapper.delete(id);
    }

    @Override
    public List<UserFile> getAllFileInfoList(){
        return userFileMapper.getAllFileInfo();
    }

    // For Database
    public boolean copyToRemoteHost(Integer file_id) throws IOException {
        FTPClient ftpClient = new FTPClient();
        //设置登陆超时时间,默认是20s
        ftpClient.setDataTimeout(12000);
        //1.连接服务器
        ftpClient.connect(remoteHost, remotePort);
        //2.登录（指定用户名和密码）
        boolean b = ftpClient.login(remoteUsername, remotePassword);
        if(!b) {
            System.out.println("登录超时");
            if (ftpClient.isConnected()) {
                // 断开连接
                ftpClient.disconnect();
            }
        }
        // 设置字符编码
        ftpClient.setControlEncoding("UTF-8");
        //基本路径，一定存在
        String basePath="/";
        String[] pathArray = remoteDatabasePath.split("/");
        for(String path:pathArray){
            basePath += path + "/";
            //3.指定目录 返回布尔类型 true表示该目录存在
            boolean dirExists = ftpClient.changeWorkingDirectory(basePath);
            //4.如果指定的目录不存在，则创建目录
            if(!dirExists){
                // 此方式，每次，只能创建一级目录
                boolean flag = ftpClient.makeDirectory(basePath);
                if (flag){
                    System.out.println("创建成功！");
                }
            }
        }
        //重新指定上传文件的路径
        ftpClient.changeWorkingDirectory(remoteDatabasePath);
        //5.设置上传文件的方式
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        UserFile userFile = userFileMapper.getLocalRealPath(file_id);
        String newFileName = userFile.getNew_file_name();
        String localRealPath = ResourceUtils.getURL("classpath:").getPath() +
                userFile.getRelative_path() + newFileName;
        File uploadFile = new File(localRealPath);


        /**
         * 6.执行上传
         * remote 上传服务后，文件的名称
         * local 文件输入流
         * 上传文件时，如果已经存在同名文件，会被覆盖
         */
        try(InputStream input =  new FileInputStream(uploadFile)) {
            return uploadFile(ftpClient,  newFileName, input);
        } catch (IOException e) {
            log.error("文件上传失败: " + e);
            return false;
        }

    }


    /**
     * 上传文件
     * @param fileName 上传到ftp的文件名
     * @param inputStream 输入文件流
     * @return
     */
    private boolean uploadFile(FTPClient ftpClient, String fileName, InputStream inputStream) {
        try {
            log.info("开始上传文件");
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            log.info("上传文件成功");
        } catch (Exception e) {
            log.error("上传文件失败" + e);
        } finally {
            try {
                if (ftpClient.isConnected())
                    ftpClient.disconnect();
                if (null != inputStream)
                    inputStream.close();
            } catch (IOException e) {
                log.error("上传文件失败: " + e);
                return false;
            }
        }
        return true;
    }

    @Override
    public Integer setFileIsRemoteStatusAndPath(Integer file_id, Boolean status, String remote_full_path) {
        return userFileMapper.setIsInRemoteStatusAndPath(file_id, status, remote_full_path);
    }

    @Override
    public List<UserFile> getRemoteFileInfoList(){
        return userFileMapper.getRemoteFileInfo();
    }

}
