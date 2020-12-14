package ooad.demo.Service;


import com.jcraft.jsch.JSchException;
import ooad.demo.pojo.Question;

import java.io.IOException;

public interface JudgeService {
    void judgeCodeDocker(int record_id,
                         Question question, String code) throws IOException, JSchException;
}
