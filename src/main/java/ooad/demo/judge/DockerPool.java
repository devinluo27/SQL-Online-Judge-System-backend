package ooad.demo.judge;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.ArrayList;

public class DockerPool {
    int poolSize;
    int DockerSeq;
    int ID;
    int DATABASE;
    String FileName;
    String FilePATH;
    String[] CMD;
    volatile ArrayList<String>  runningList = new ArrayList<>();
    volatile ArrayList<String> sleepingList = new ArrayList<>();
    final Object fillDockerPoolLock = new Object();

    public Object getFillDockerPoolLockReach0() {
        return fillDockerPoolLockReach0;
    }

    final Object fillDockerPoolLockReach0 = new Object();

    ArrayList<String> availableList = new ArrayList<>();

    public Object getFillDockerPoolLock() {
        return fillDockerPoolLock;
    }

    public ArrayList<String> getAvailableList() {
        return availableList;
    }

    public int getPoolSize() {
        return poolSize;
    }

    static String KillDockerCMD = "docker kill #DockerNAME#";
    static String RemoveDockerCMD = "docker rm -f #DockerNAME#";
    static String AwakeDockerCMD = "docker start #DockerNAME#";
    static String[] DockerDB = {
            "postgres"
    };

    static String[][] DockerCMD = { {
                "docker run --name #DockerNAME# -e POSTGRES_PASSWORD=SQL_tester -d postgres",
                    "docker cp #DockerFilePATH# #DockerNAME#:/data.sql",
                    "sleep 2",
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
                    "docker cp #FilePATH# #DockerNAME#:/data.sqlite"
            }, {
    }};



    public DockerPool(int DockerSeq, int ID, int DATABASE, String FileName, String FilePATH) throws IOException, JSchException {
        this.DockerSeq = 0;
        this.poolSize = DockerSeq;
        this.ID = ID;
        this.DATABASE = DATABASE;
        CMD = DockerCMD[DATABASE];
        this.FileName = FileName;
        this.FilePATH = FilePATH;
        this.InitDockerPool(DockerSeq);
    }

    public Remote.Log KillDocker(String DockerName) throws IOException, JSchException {
        runningList.remove(DockerName);
        if (!sleepingList.contains(DockerName)) sleepingList.add(DockerName);
        return Remote.EXEC_CMD(new String[]{KillDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public Remote.Log RemoveDocker(String DockerName) throws IOException, JSchException {
        runningList.remove(DockerName);
        sleepingList.remove(DockerName);
        return Remote.EXEC_CMD(new String[]{RemoveDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public Remote.Log RemoveDockerOnly(String DockerName) throws IOException, JSchException {
        return Remote.EXEC_CMD(new String[]{RemoveDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }


    public Remote.Log AwakeDocker(String DockerName) throws IOException, JSchException {
        if (!runningList.contains(DockerName)) runningList.add(DockerName);
        sleepingList.remove(DockerName);
        return Remote.EXEC_CMD(new String[]{AwakeDockerCMD.replaceAll("#DockerNAME#", DockerName)}).get(0);
    }

    public ArrayList<Remote.Log> BuildDocker(String[] CMD, String DockerName) throws IOException, JSchException {
        for (int j = 0; j < CMD.length; j++) CMD[j] = CMD[j].replaceAll("#DockerNAME#", DockerName);
        runningList.add(DockerName);
        return Remote.EXEC_CMD(CMD);
    }


    public ArrayList<String> getSleepingList() {
        return sleepingList;
    }

    public ArrayList<ArrayList<Remote.Log>> InitDockerPool(int num) throws IOException, JSchException {
//        String[] CMD = DockerCMD[DATABASE];
        ArrayList<ArrayList<Remote.Log>> Logs = new ArrayList<>();

        for (int i = 0; i < CMD.length; i++)
            CMD[i] = CMD[i].replaceAll("#DockerFilePATH#", FilePATH).replaceAll("#DockerFileNAME#", FileName);
        for (int i = 0; i < num; i++) {
            String DockerName = "DBOJ" + "_" + DockerDB[DATABASE] + "_" + FileName + "_" + ID + "_" + ++DockerSeq;
            Logs.add(BuildDocker(CMD.clone(), DockerName));
        }
        return Logs;
    }


    public void rebuildDocker(int num) throws IOException, JSchException {
        ArrayList<String> names = refillDockersOnly(num);
        refillRunningListOnly(names);
    }

    public ArrayList<String> refillDockersOnly(int num) throws IOException, JSchException {
        ArrayList<ArrayList<Remote.Log>> Logs = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            System.out.println("running list: " + runningList);
            String DockerName = "DBOJ" + "_" + DockerDB[DATABASE] + "_" + FileName + "_" + ID + "_" + ++DockerSeq;
            Logs.add(createDockerOnly(CMD.clone(), DockerName));
            names.add(DockerName);
        }
        return names;
    }

    public ArrayList<Remote.Log> createDockerOnly(String[] CMD, String DockerName) throws IOException, JSchException {
        for (int j = 0; j < CMD.length; j++) CMD[j] = CMD[j].replaceAll("#DockerNAME#", DockerName);
        return Remote.EXEC_CMD(CMD);
    }

    public synchronized void refillRunningListOnly(ArrayList<String> names){
        runningList.addAll(names);
    }

    public ArrayList<String> getRunningList() {
        return runningList;
    }
}