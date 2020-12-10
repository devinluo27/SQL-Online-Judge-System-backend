package ooad.demo.judge;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.ArrayList;
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
    }
    @Value("${judge.remoteHost}")
    private String remoteHost = "10.20.83.122";

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
}
