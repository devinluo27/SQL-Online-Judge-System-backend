package ooad.demo;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import org.junit.Test;

import java.io.IOException;

public class HealthyTest {
    public static void main(String[] args) throws IOException, JSchException {
        String dockerNAME = "test-mysql";
//        System.out.println(DockerPool.HealthyCheck(dockerNAME,"running"));
//        System.out.println(DockerPool.checkIfRunning(dockerNAME));
    }

}