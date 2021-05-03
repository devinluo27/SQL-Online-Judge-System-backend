package ooad.demo.judge;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
public class Judge {

    @Autowired
    Remote remote;

    public static class QUERY_RESULT {
        int score = 0;
        double exec_time = -1;
        String OUT = "";
        String ERROR = "";

        public QUERY_RESULT(int score) {
            this.score = score;
        }

        public QUERY_RESULT(int score, double exec_time, String OUT, String ERROR) {
            this.score = score;
            this.exec_time = exec_time;
            this.OUT = OUT;
            this.ERROR = ERROR;
        }

        public int getScore() {
            return score;
        }

        public double getExec_time() {
            return exec_time;
        }

        public String getOUT() {
            return OUT;
        }

        public String getERROR() {
            return ERROR;
        }
    }

    static String[] QUERY_CONFIG = {
            ", row_number() over ()",
            ", row_number() over ()",
            ", row_number() over ()"
    };

    static String[] QUERY_SQL = {
            "docker exec #DockerNAME# psql -U tester -d task -t -c \"\\timing on\" -c  \" " +
                    "select count(*) from (\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #ANS_SQL# ) STAND_ANS1  except\n" +
                    "      select * #CONFIG# from ( #TEST_SQL# ) INPUT_ANS1) EXCEPT_ANS1\n" +
                    "UNION ALL\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #TEST_SQL# ) INPUT_SQL2 except\n" +
                    "      select * #CONFIG# from ( #ANS_SQL# ) STANDARD_SQL2)\n" +
                    "    as EXCEPT_ANS2) as JUDGE;" +
                    "\" ",

            "docker exec #DockerNAME# sqlite3 data.sqlite -readonly \" " +
                    "select count(*) from (\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #ANS_SQL# ) STAND_ANS1  except\n" +
                    "      select * #CONFIG# from ( #TEST_SQL# ) INPUT_ANS1) EXCEPT_ANS1\n" +
                    "UNION ALL\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #TEST_SQL# ) INPUT_SQL2 except\n" +
                    "      select * #CONFIG# from ( #ANS_SQL# ) STANDARD_SQL2)\n" +
                    "    as EXCEPT_ANS2) as JUDGE;" +
                    "\" ",

            "docker exec test-mysql  mysql  -h localhost -u tester  -D TASK -e \"" +
                    "select UNION_CNT - ANS_CNT from (\n" +
                    "select count(*) as UNION_CNT, count(*) as ANS_CNT\n" +
                    "from (\n" +
                    "         select * #CONFIG# \n" +
                    "         from ( #TEST_SQL# ) as TEST_SQL\n" +
                    "         union\n" +
                    "         select * #CONFIG#\n" +
                    "         from ( #ANS_SQL# ) as ANS_SQL\n" +
                    "     ) as UNION_SET ,( #ANS_SQL# )as ANS_SET) as RST;\" "
    };

    static String[][] TRIGGER_SQL = {{
            "docker cp #TEST_DATA_PATH# #DockerNAME#:/TEST_DATA.sql",
            "docker cp #ANS_TABLE_PATH# #DockerNAME#:/ANS_TABLE.sql",
            "docker exec #DockerNAME# psql -U postgres -d task -f ANS_TABLE.sql",
            "sleep 1",
            "docker exec #DockerNAME# psql -U postgres -d task -c \" CREATE USER checker WITH PASSWORD 'check_for_trigger'; \" ",
            "docker exec #DockerNAME# psql -U postgres -d task -c \" GRANT ALL ON ALL TABLES IN SCHEMA public TO checker; \" ",
            "docker exec #DockerNAME# psql -U postgres -d task -c \" REVOKE ALL ON TABLE answer FROM checker; \" ",
            "docker exec #DockerNAME# psql -U checker  -d task -c \"  #TEST_SQL# \" ",
            "docker exec #DockerNAME# psql -U postgres -d task -f TEST_DATA.sql",
            "sleep 1",
            "docker exec #DockerNAME# psql -U postgres -d task -t -c \" " +
                    "select (case when cast(100*(1-1.0*ERROR_CNT/#TEST_CONFIG#) as int) < 0 then 0 " +
                    "ELSE cast(100*(1-1.0*ERROR_CNT/#TEST_CONFIG#) as int) end) as SCORE from (\n" +
                    "select count(JUDGE) as ERROR_CNT \n" +
                    "from (\n" +
                    "         select *\n" +
                    "         from (select *\n" +
                    "               from (select * from #TARGET_TABLE#) INPUT1 except\n" +
                    "               select *\n" +
                    "               from (select * from answer) STANDARD1) EXCEPT_ANS1\n" +
                    "         UNION ALL\n" +
                    "         select *\n" +
                    "         from (select *\n" +
                    "               from (select * from answer) STANDARD2 except\n" +
                    "               select *\n" +
                    "               from (select * from #TARGET_TABLE#) INPUT2)\n" +
                    "                  as EXCEPT_ANS2 ) as JUDGE\n" +
                    "\n" +
                    "    ) TESTER_POINT \"",
    }};


    public QUERY_RESULT EXEC_QUERY(String ANS_SQL, String TEST_SQL, String DockerName, boolean Ordered, int DBMS) throws IOException, JSchException {
        String CMD = QUERY_SQL[DBMS];
        CMD = Ordered ? CMD.replaceAll("#CONFIG#", QUERY_CONFIG[DBMS]) : CMD.replaceAll("#CONFIG#", "");
        CMD = CMD.replaceAll("#DockerNAME#", DockerName).replaceAll("#ANS_SQL#", ANS_SQL).replaceAll("#TEST_SQL#", TEST_SQL);
        Remote.Log logs = new Remote.Log(-1, "" ,"");
        try {
            // TODO: how to support many languages
            logs = Remote.EXEC_CMD(new String[]{CMD}).get(0);
        } catch (Exception e){
            log.error("EXEC_QUERY: ", e);
            return new QUERY_RESULT(-2, -1.0, logs.OUT, logs.ERROR);
        }
//        System.out.println("OUT: " + logs.OUT);
//        System.out.println("ERROR: " + logs.ERROR);
        String[] result = logs.OUT.split("\n");
        int SCORE = 0;
        double EXEC_TIME = 0;
        if (DBMS == 0) {
            // TODO: DEBUGGING HERE
            if (result.length != 4) {
                System.out.println("Here != 4");
                for (int i = 0; i<result.length;i++){
                    System.out.println(result[i]);
                }
                return new QUERY_RESULT(-1, -1, logs.OUT, logs.ERROR);
            }
            SCORE = Integer.parseInt(result[1].replaceAll(" ", "")) == 0 ? 100 : 0;
            EXEC_TIME = Double.parseDouble(result[3].replaceAll("Time: ", "").replaceAll(" ms", ""));
        }else if(DBMS == 1){
            SCORE = Integer.parseInt(result[0].replaceAll(" ", "")) == 0 ? 100 : 0;
            EXEC_TIME = Double.parseDouble(result[3].replaceAll("Time: ", "").replaceAll(" ms", ""));
//            EXEC_TIME = (double) logs.exec_time - 300;
        }else if (DBMS == 2){
            SCORE = Integer.parseInt(result[1].replaceAll(" ", "")) == 0 ? 100 : 0;
            EXEC_TIME = Double.parseDouble(result[3].replaceAll("Time: ", "").replaceAll(" ms", ""));
//            EXEC_TIME = (double) logs.exec_time - 300;
        }
        System.out.println(SCORE + "  " + EXEC_TIME);
        return new QUERY_RESULT(SCORE, EXEC_TIME, logs.OUT, logs.ERROR);
    }

    public QUERY_RESULT EXEC_TRIGGER(String ANS_TABLE_PATH, String TEST_SQL, String TEST_DATA_PATH, int TEST_CONFIG, String DockerName, int DBMS, String TARGET_TABLE)
            throws IOException, JSchException {

        TEST_SQL = TEST_SQL.replaceAll("\\$\\$", "####");
        TEST_SQL = java.util.regex.Matcher.quoteReplacement(TEST_SQL);
        // TODO: Deepcopy is required.
        String[] CMD = Arrays.copyOf(TRIGGER_SQL[DBMS], TRIGGER_SQL[DBMS].length);
        System.out.println("EXEC_TRIGGER: " + DockerName);
        for (int i = 0; i < CMD.length; i++){
            CMD[i] = CMD[i]
                    .replaceAll("#DockerNAME#", DockerName)
                    .replaceAll("#TEST_DATA_PATH#", TEST_DATA_PATH)
                    .replaceAll("#ANS_TABLE_PATH#", ANS_TABLE_PATH)
                    .replaceAll("#TARGET_TABLE#", TARGET_TABLE)
                    .replaceAll("#TEST_CONFIG#", String.valueOf(Math.max(TEST_CONFIG, 1)))
                    .replaceAll("#TEST_SQL#", TEST_SQL)
                    .replaceAll("####", " \\\\\\$\\$ ");
        }

        int score = 0;
        ArrayList<Remote.Log> logs = new ArrayList<>();
        try {
            logs = remote.EXEC_CMD(CMD);
            score = logs.get(logs.size() - 1).OUT != null
                    && logs.get(logs.size() - 1).OUT.length() > 0 ?
                    Math.max(Integer.parseInt(logs.get(logs.size() - 1).OUT
                            .replaceAll(" ", "")
                            .replaceAll("\n", "")), 0) : 0;
        } catch (Exception e){
            score = -4; // 判题机发生异常！！！
            e.printStackTrace();
            return new QUERY_RESULT(score);
        }
        return new QUERY_RESULT(score, (double) logs.get(logs.size() - 3).exec_time, logs.get(logs.size() - 4).OUT + "\n" + logs.get(logs.size() - 3).OUT + "\n" + logs.get(logs.size() - 3).ERROR, logs.get(logs.size() - 4).ERROR);
    }
}

