package ooad.demo.judge;

import com.jcraft.jsch.*;
import ooad.demo.mapper.UserFileMapper;
import ooad.demo.pojo.UserFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class Remote {

    public static class Log{
        long exec_time;
        String OUT;
        String ERROR;
        public Log(long exec_time, String OUT, String ERROR) {
            this.exec_time = exec_time;
            this.OUT = OUT;
            this.ERROR = ERROR;
        }

        public String getOUT() {
            return OUT;
        }
    }
    @Autowired
    UserFileMapper userFileMapper;

    @Value("${judge.remote.host}")
    private String remoteHost = "10.20.83.122";

    @Value("${judge.remote.port}")
    private int remotePort = 22;

    @Value("${judge.remote.username}")
    private String remoteUsername = "dboj_manager";

    @Value("${judge.remote.password}")
    private String remotePassword = "789ab73077cb";

    @Value("${judge.remote.database-path}")
    private final String remoteDatabasePath = "/data2/DBOJ/DockerTest/";

    @Value("${judge.remote.file-path}")
    private final String remoteFilePath = "/data2/DBOJ/JudgeFile/";

    private  String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private String SessionStrictHostKeyChecking = "no";

    private String protocol = "sftp";



    private int channelConnectedTimeout = 15000;

    private int sessionConnectTimeout = 15000;


    public static ArrayList<Log> EXEC_CMD(String[] CMD) throws JSchException, IOException {
        String host =  "10.20.83.122";
        int port = 22;
        String userName = "dboj_manager";
        String password = "789ab73077cb";
        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(6000);
        session.connect();
        ArrayList<Log> result = new ArrayList<>();
        for (String cmd : CMD) {
            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            InputStream in = exec.getInputStream();
            InputStream error = exec.getErrStream();
            long start_time = System.currentTimeMillis();
            exec.setCommand(cmd);
            exec.connect();
            String IN = IOUtils.toString(in, "UTF-8");
            String ERROR = IOUtils.toString(error, "UTF-8");
            result.add(new Log(System.currentTimeMillis() - start_time - 300, IN, ERROR));
//            System.out.println("=============OUT MSG================");
//            System.out.println(IN);
//            System.out.println("=============ERROR MSG==============");
            System.out.println(ERROR);
//            System.out.println("=====================================\n" +
//                               "=====================================\n");
            in.close();
        }
        session.disconnect();
        return result;
    }

    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session = null;

        if (port <= 0) {
            session = jsch.getSession(username, host);
        } else {
            session = jsch.getSession(username, host, port);
        }

        if (session == null) {
            throw new Exception(host + " session is null");
        }

        session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, SessionStrictHostKeyChecking);
        return session;
    }



    public boolean uploadFile(int file_id, String targetFullPath) throws Exception {
        UserFile userFile = userFileMapper.getLocalRealPath(file_id);
        String newFileName = userFile.getNew_file_name();
        String localRealPath = ResourceUtils.getURL("classpath:").getPath() +
                userFile.getRelative_path() + newFileName;
        File uploadFile = new File(localRealPath);
//        String targetPath = remoteDatabasePath + newFileName;
        try(InputStream input =  new FileInputStream(uploadFile)) {
            return this.uploadFileSSH(targetFullPath, input);
        } catch (FileNotFoundException e) {
            System.out.println(e);
//            log.error("文件上传失败"+e);
            System.out.println("uploadFile Here!");
            return false;
        } catch (IOException e) {
            System.out.println(e);
//            log.error("文件上传失败" + e);
            return false;
        }
    }

        /***
         * targetPath 包括了文件名和后缀等信息
         * @param targetPath
         * @param inputStream
         * @return
         * @throws Exception
         */
    private boolean uploadFileSSH(String targetPath, InputStream inputStream) throws Exception {

        ChannelSftp sftp = this.createSftp();
        try {
            String rootDir = "/";
            sftp.cd(rootDir);
//            log.info("Change path to {}", config.getRoot());
            int index = targetPath.lastIndexOf("/");
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = this.createDirs(fileDir, sftp);
            if (!dirs) {
//                log.error("Remote path error. path:{}", targetPath);
                throw new Exception("Upload File failure");
            }
            System.out.println("reach sftp.put");
            sftp.put(inputStream, fileName);
            return true;
        } catch (IOException e) {
//            log.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Upload File failure" + "Upload file failure. TargetPath: {}" + targetPath);
        } finally {
            this.disconnect(sftp);
        }
    }


    private ChannelSftp createSftp() throws Exception {

        String host =  "10.20.83.122";
        int port = 22;
        String userName = "dboj_manager";
        String password = "789ab73077cb";
        int time_out = 10000;

        JSch jsch = new JSch();
//        log.info("Try to connect sftp[" + userName + "@" + config.getHost() + "], use password[" + config.getPassword() + "]");

        Session session = createSession(jsch, host, userName, port);
        session.setPassword(password);
        session.connect(time_out);

//        log.info("Session connected to {}.", host);

//        Channel channel = session.openChannel(config.getProtocol());
//        channel.connect(config.getChannelConnectedTimeout());
        Channel channel = session.openChannel(protocol);
        channel.connect();

//        log.info("Channel created to {}.", host);

        return (ChannelSftp) channel;
    }



    private boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .toArray(String[]::new);

            for (String dir : dirs) {
                try {
                    sftp.cd(dir);
//                    log.info("Change directory {}", dir);
                } catch (Exception e) {
                    try {
                        sftp.mkdir(dir);
//                        log.info("Create directory {}", dir);
                    } catch (SftpException e1) {
//                        log.error("Create directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                    try {
                        sftp.cd(dir);
//                        log.info("Change directory {}", dir);
                    } catch (SftpException e1) {
//                        log.error("Change directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }


    private void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
//                    log.info("sftp is closed already");
                }
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

}
