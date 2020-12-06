package ooad.demo.Service;


import com.jcraft.jsch.JSchException;

import java.io.IOException;

public interface JudgeService {
    void judgeCodeDocker(int record_id, String question_id,
                         String standard_ans, String code, boolean isOrder,
                         int operation_type, String type) throws IOException, JSchException;
}
