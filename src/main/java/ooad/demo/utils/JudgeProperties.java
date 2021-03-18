package ooad.demo.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
//@ConfigurationProperties(ignoreUnknownFields = false, prefix = "judge")
public class JudgeProperties {

    @Value("${judge.dockerPool.docker.num}")
    private int docker_num;

    @Value("${judge.remote.host}")
    private String remoteHost;

    @Value("${judge.remote.port}")
    private int remotePort;

    @Value("${judge.remote.rootDir}")
    private String rootDir;

    @Value("${judge.remote.username}")
    private String remoteUsername;

    @Value("${judge.remote.password}")
    private String remotePassword;

    @Value("${judge.remote.database-path}")
    private String remoteDatabasePath;

    @Value("${judge.remote.file-path}")
    private String remoteFilePath;

}
