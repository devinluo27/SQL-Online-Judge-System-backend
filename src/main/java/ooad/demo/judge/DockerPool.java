package ooad.demo.judge;

import com.jcraft.jsch.JSchException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

/* TODO: Something to fix there
*   DockerPool is created by new, so cannot Autowired anything! */

@Slf4j
@Service
@Data
public class DockerPool {
    int poolSize;
    int DockerSeq;
    int ID;
    int DATABASE;

    String FileName;
    String FilePATH;
    String[] CMD;

    // TODO: volatile 保护的是指向数组的引用
    final ArrayList<Docker>  runningList = new ArrayList<>();
    volatile ArrayList<Docker> sleepingList = new ArrayList<>();
    volatile ArrayList<Docker> executingList = new ArrayList<>();

//    @Autowired
//    private Remote remote;

    private String KillDockerCMD = "docker kill #DockerNAME#";
    private String RemoveDockerCMD = "docker rm -f #DockerNAME#";
    private String AwakeDockerCMD = "docker start #DockerNAME#";

    private String HealthyCheckCMD = "docker ps --filter name=#DockerNAME# --filter status=#STATUS# --format \"{{.Names}}\"";

    private static String[] DockerDB = {
            "postgres",
            "sqlite",
            "mysql"
    };

    // --device-write-bps /dev/sda:66MB
    private String[][] DockerCMD = { {
                "docker run -m 500M --cpus 2 --name #DockerNAME# -e POSTGRES_PASSWORD=SQL_tester -d postgres",
                    "docker cp #DockerFilePATH# #DockerNAME#:/data.sql",
                    "sleep 3",
                    "docker exec #DockerNAME# psql -U postgres -c \" create database task; \" ",
                    "docker exec #DockerNAME# psql -U postgres -d task -f data.sql -q",
                    "docker exec #DockerNAME# psql -U postgres -d task -c \" " +
                            "CREATE USER tester WITH ENCRYPTED PASSWORD 'tester_read_only!';" +
                            "ALTER USER tester SET default_transaction_read_only = on;" +
                            "GRANT USAGE ON SCHEMA public to tester;" +
                            "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO tester;" +
                            "GRANT CONNECT ON DATABASE task TO tester;" +
                            "GRANT SELECT ON ALL TABLES IN SCHEMA public TO tester;\" "
            }, {
                "docker run --name #DockerNAME# -dit keinos/sqlite3",
                    "docker cp #DockerFilePATH# #DockerNAME#:/data.sqlite"
            }, {
                "docker run --name #DockerNAME# -e MYSQL_ROOT_PASSWORD=123123 -d mysql",
            "sleep 20",
            "docker cp #DockerFilePATH# #DockerNAME#:/data.sql",
            "docker exec #DockerNAME#  mysql  -h localhost -u root --password=123123 -e \"create database TASK\"",
            "docker exec #DockerNAME#  mysql  -h localhost -u root --password=123123 -D TASK -e\"source data.sql\"",
            "docker exec #DockerNAME#  mysql  -h localhost -u root --password=123123 -e \"create user tester; \"",
            "docker exec #DockerNAME#  mysql  -h localhost -u root --password=123123 -e \"grant select on TASK.* to 'tester'@'%'\""
    }};

    public DockerPool(){

    }

    /***
     *  DBMS: 0 Postgresql, 1: SQLite, 2: MySQL
     *  automatically creates dockers in remote server
     * @param DockerSeq
     * @param ID
     * @param DBMS
     * @param FileName
     * @param FilePATH
     * @throws IOException
     * @throws JSchException
     */
    public DockerPool(int DockerSeq, int ID, int DBMS, String FileName, String FilePATH) throws IOException, JSchException {
        System.out.println(DockerSeq);
        this.DockerSeq = 0;
        this.poolSize = DockerSeq;
        this.ID = ID;
        this.DATABASE = DBMS;
        CMD = DockerCMD[DBMS];
        this.FileName = FileName;
        this.FilePATH = FilePATH;
        this.InitDockerPool(DockerSeq);
    }


    Remote.Log killDocker(String DockerName) throws IOException, JSchException {
        runningList.remove(DockerName);
        if (!sleepingList.contains(DockerName)) sleepingList.add(new Docker(DockerName));
        return Remote.EXEC_CMD(new String[]{KillDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public Remote.Log RemoveDocker(String DockerName) throws IOException, JSchException {
//        runningList.remove(DockerName);
//        sleepingList.remove(DockerName);
        return Remote.EXEC_CMD(new String[]{RemoveDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public Remote.Log RemoveDockerOnly(String DockerName) throws IOException, JSchException {
        if (DockerName != null)
            return Remote.EXEC_CMD(new String[]{RemoveDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
        return null;
    }


    public Remote.Log AwakeDocker(String DockerName) throws IOException, JSchException {
        if (!runningList.contains(DockerName)) runningList.add(new Docker(DockerName));
        sleepingList.remove(DockerName);
        return Remote.EXEC_CMD(new String[]{AwakeDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public ArrayList<Remote.Log> BuildDocker(String[] CMD, String DockerName) throws JSchException, IOException {
        for (int j = 0; j < CMD.length; j++) CMD[j] = CMD[j].replaceAll("#DockerNAME#", DockerName);

        runningList.add(new Docker(DockerName));
        // TODO: Test
//        System.out.println("REMOTE: " + Remote);
//        System.out.println("CMD:" + CMD);
        return Remote.EXEC_CMD(CMD);
    }

    public ArrayList<ArrayList<Remote.Log>> InitDockerPool(int num) throws IOException, JSchException {
        ArrayList<ArrayList<Remote.Log>> Logs = new ArrayList<>();

        for (int i = 0; i < CMD.length; i++)
            CMD[i] = CMD[i].replaceAll("#DockerFilePATH#", FilePATH).replaceAll("#DockerFileNAME#", FileName);

        for (int i = 0; i < num; i++) {
            String DockerName = "DBOJ" + "_" + DockerDB[DATABASE] + "_" + FileName + "_" + ID + "_" + ++DockerSeq;
            Logs.add(BuildDocker(CMD.clone(), DockerName));
        }
        return Logs;
    }


    // ===== functions series for rebuild in trigger judging =====
    public int rebuildDocker(int num) throws IOException, JSchException {
        refillDockersAndUpdateList(num);
        return 1;
    }

    public void refillDockersAndUpdateList(int num) throws IOException, JSchException {
        ArrayList<ArrayList<Remote.Log>> Logs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String DockerName = "DBOJ" + "_" + DockerDB[DATABASE] + "_" + FileName + "_" + ID + "_" + ++DockerSeq;
            Logs.add(createDockerOnly(CMD.clone(), DockerName));
            refillRunningListOnly(DockerName);
        }
    }

    public ArrayList<Remote.Log> createDockerOnly(String[] CMD, String DockerName) throws IOException, JSchException {
        for (int j = 0; j < CMD.length; j++) CMD[j] = CMD[j].replaceAll("#DockerNAME#", DockerName);
        return Remote.EXEC_CMD(CMD);
    }


    // TODO: ADD a docker to RunningList 并唤醒一个wait()的线程
    public void refillRunningListOnly(String DockerName){
        synchronized (runningList){
            runningList.add(new Docker(DockerName));
            System.out.println("After ADDING Running list: " + runningList.size());
            runningList.notify();
        }
    }


    static String checkIfRunningCMD = "docker ps --filter name=#DockerNAME# --filter status=running --format \"{{.Names}}\"";

    // TODO: THE SAME
    public boolean checkIfRunning(String DockerNAME) throws IOException, JSchException {
        String CMD = checkIfRunningCMD.replaceAll("#DockerNAME#",DockerNAME);
        Remote.Log log = Remote.EXEC_CMD(new String[]{CMD}).get(0);
        return log.OUT != null && log.OUT.replaceAll("\n","").equals(DockerNAME);
    }

    // TODO:
    private boolean HealthyCheck(String DockerNAME, String STATUS) throws IOException, JSchException {
        String CMD = HealthyCheckCMD.replaceAll("#DockerNAME#",DockerNAME).replaceAll("#STATUS#", STATUS);
        Remote.Log log = Remote.EXEC_CMD(new String[]{CMD}).get(0);
        return log.OUT != null && log.OUT.replaceAll("\n","").equals(DockerNAME);
    }

}