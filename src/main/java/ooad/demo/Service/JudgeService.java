package ooad.demo.Service;


import com.jcraft.jsch.JSchException;

import java.io.IOException;

public interface JudgeService {
    void judgeCodeDocker(int record_id, Integer question_id,
                          String code, boolean isOrder,
                          String type) throws IOException, JSchException;
}
