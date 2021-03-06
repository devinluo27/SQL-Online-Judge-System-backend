package ooad.demo.judge;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.mapper.UserFileMapper;
import ooad.demo.pojo.UserFile;
import ooad.demo.utils.JudgeProperties;
import ooad.demo.utils.SftpProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
public class Remote {

    public static class Log{
        public long exec_time;
        public String OUT;
        public String ERROR;
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

    @Autowired
    JudgeProperties judgeProperties;

    private static String remoteHost;

    private static int remotePort;

    private static String rootDir;

    private static String remoteUsername;

    private static String remotePassword;

    private static String remoteDatabasePath;

    private static String remoteFilePath;

    // TODO: TO BE COMPLETE

    //    @Autowired
    //    private SftpProperties sftpConfig;
    private  String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private String SessionStrictHostKeyChecking = "no";

    private String protocol = "sftp";

    private String sftpRoot = "/";

    private int channelConnectedTimeout = 15000;

    private int sessionConnectTimeout = 15000;

    @PostConstruct
    public void postInit(){
        remoteHost = judgeProperties.getRemoteHost();
        remotePort = judgeProperties.getRemotePort();
        rootDir = judgeProperties.getRootDir();
        remoteUsername = judgeProperties.getRemoteUsername();
        remotePassword = judgeProperties.getRemotePassword();
        remoteDatabasePath = judgeProperties.getRemoteDatabasePath();
        remoteFilePath = judgeProperties.getRemoteFilePath();
    }

    public static ArrayList<Log> EXEC_CMD(String[] CMD) throws JSchException, IOException {
        String host =  remoteHost;
        int port = remotePort;
        String userName = remoteUsername;
        String password = remotePassword;
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
            result.add(new Log(System.currentTimeMillis() - start_time, IN, ERROR));

            // TODO: ERROR PRINT
//            ERROR = ERROR.replace("\n","").trim();
//            if (ERROR.length() >= 30)
//                log.warn(ERROR.substring(0,30));
//            else {
//                log.warn(ERROR);
//            }
//            System.out.println(ERROR);

            in.close();
        }
        session.disconnect();
        return result;
    }

    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session;
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
            return this.uploadFileSftp(targetFullPath, input);
        } catch (FileNotFoundException e) {
            log.error("????????????????????????????????????"+ e);
            System.out.println("uploadFile Here!");
            return false;
        } catch (IOException e) {
            log.error("??????????????????" + e);
            return false;
        }
    }

        /***
         * targetPath ????????????????????????????????????
         * @param targetPath
         * @param inputStream
         * @return
         * @throws Exception
         */
    private boolean uploadFileSftp(String targetPath, InputStream inputStream) throws Exception {

        ChannelSftp sftp = this.createSftp();
        try {
            String rootDir = "/";
            sftp.cd(rootDir);
            log.info("Change path to {}", rootDir);
            int index = targetPath.lastIndexOf("/");
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = this.createDirs(fileDir, sftp);
            if (!dirs) {
                log.error("Remote path error. path:{}", targetPath);
                throw new Exception("Upload File failure");
            }
            System.out.println("reach sftp.put");
            sftp.put(inputStream, fileName);
            return true;
        } catch (IOException e) {
            log.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Upload File failure" + "Upload file failure. TargetPath: {}" + targetPath);
        } finally {
            this.disconnect(sftp);
        }
    }


    private ChannelSftp createSftp() throws Exception {
        String host =  remoteHost;
        int port = remotePort;
        String userName = remoteUsername;
        String password = remotePassword;
        int time_out = 10000;

        JSch jsch = new JSch();
        log.info("Try to connect sftp[" + userName + "@" + remoteHost + "], use password[" + password + "]");

        Session session = createSession(jsch, host, userName, port);
        session.setPassword(password);
        // one new line here
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(time_out);

        log.info("Session connected to {}.", host);

//        Channel channel = session.openChannel(config.getProtocol());
//        channel.connect(config.getChannelConnectedTimeout());
        Channel channel = session.openChannel(protocol);
        channel.connect();
        log.info("Channel created to {}.", host);
        return (ChannelSftp) channel;
    }


    /***
     * ??????????????????????????????????????????
     * @param dirPath
     * @param sftp
     * @return
     */
    private boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .toArray(String[]::new);

            for (String dir : dirs) {
                try {
                    sftp.cd(dir);
                    log.info("Change directory {}", dir);
                } catch (Exception e) {
                    try {
                        sftp.mkdir(dir);
                        log.info("Create directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Create directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                    try {
                        sftp.cd(dir);
                        log.info("Change directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Change directory failure, directory:{}", dir, e1);
                        e1.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }


    public boolean deleteFileSftp(String targetFullPath) throws Exception {
        ChannelSftp sftp = null;
        try {
            sftp = this.createSftp();
            sftp.cd(sftpRoot);
            sftp.rm(targetFullPath);
            return true;
        } catch (Exception e) {
            log.error("Delete file failure. TargetPath: {}", targetFullPath, e);
            throw new Exception("Delete File failure");
        } finally {
            this.disconnect(sftp);
        }
    }

    public File downloadFileSftp(String targetPath) throws Exception {
        ChannelSftp sftp = this.createSftp();
        OutputStream outputStream = null;
        try {
            // TODO: switch
//            sftp.cd(sftpConfig.getRoot());
//            log.info("Change path to {}", sftpConfig.getRoot());

            sftp.cd(rootDir);
            log.info("Change path to {}", rootDir);

            File file = new File(targetPath.substring(targetPath.lastIndexOf("/") + 1));

            outputStream = new FileOutputStream(file);
            sftp.get(targetPath, outputStream);
            log.info("Download file success. TargetPath: {}", targetPath);
            return file;
        } catch (Exception e) {
            log.error("Download file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Download File failure");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.disconnect(sftp);
        }
    }

    /***
     * ????????????
     * @param sftp
     */
    private void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
                    log.info("sftp is closed already");
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
