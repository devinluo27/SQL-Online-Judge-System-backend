package ooad.demo.judge;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Docker {
    Timestamp exec_start;
    String docker_name;

    public Docker(Timestamp exec_start, String docker_name) {
        this.exec_start = exec_start;
        this.docker_name = docker_name;
    }

    public Docker(String docker_name) {
        this.docker_name = docker_name;
        exec_start = new Timestamp(System.currentTimeMillis());
    }

    public Docker() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Docker)) {
            return false;
        }
        Docker d = (Docker) o;
        return this.docker_name.equals(d.docker_name);
    }
}
