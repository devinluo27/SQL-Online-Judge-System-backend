package ooad.demo.judge;

import com.jcraft.jsch.JSchException;

import java.io.*;

public class Judge {

    public static class QUERY_RESULT {
        public int score;
        public long exec_time;
        public String OUT;
        public String ERROR;

        public QUERY_RESULT(int score, long exec_time, String OUT, String ERROR) {
            this.score = score;
            this.exec_time = exec_time;
            this.OUT = OUT;
            this.ERROR = ERROR;
        }
    }

    static String[] QUERY_CONFIG = {
            ", row_number() over ()",
            ", row_number() over ()"
    };

    static String[] QUERY_SQL = {
            "docker exec #DockerNAME# psql -U tester -d task -c \" " +
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

            "docker exec #DockerNAME# sqlite3 data.sqlite \" " +
                    "select count(*) from (\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #ANS_SQL# ) STAND_ANS1  except\n" +
                    "      select * #CONFIG# from ( #TEST_SQL# ) INPUT_ANS1) EXCEPT_ANS1\n" +
                    "UNION ALL\n" +
                    "select *\n" +
                    "from (select * #CONFIG# from ( #TEST_SQL# ) INPUT_SQL2 except\n" +
                    "      select * #CONFIG# from ( #ANS_SQL# ) STANDARD_SQL2)\n" +
                    "    as EXCEPT_ANS2) as JUDGE;" +
                    "\" "
    };

    static String[][] TRIGGER_SQL = {{
                    "docker cp #TEST_DATA_PATH# #DockerNAME#:/TEST_DATA.sql",
                    "docker cp #ANS_TABLE_PATH# #DockerNAME#:/ANS_TABLE.sql",
                    "docker exec #DockerNAME# psql -U postgres -d task -f ANS_TABLE.sql",
                    "docker exec #DockerNAME# psql -U postgres -d task -c \" CREATE USER checker WITH PASSWORD 'check_for_trigger'; \" ",
                    "docker exec #DockerNAME# psql -U postgres -d task -c \" GRANT ALL ON ALL TABLES IN SCHEMA public TO checker; \" ",
                    "docker exec #DockerNAME# psql -U postgres -d task -c \" REVOKE ALL ON TABLE answer FROM checker; \" ",
                    "docker exec #DockerNAME# psql -U checker -d task -c \"  #TEST_SQL# \" ",
                    "docker exec #DockerNAME# psql -U postgres -d task -f TEST_DATA.sql",
                    "sleep 3",
                    "docker exec #DockerNAME# psql -U postgres -d task -c \" " +
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
                            "    ) TESTER_POINT \""
            },{

    }
    };

    static int[] RESULT_INDEX = {21};


    public static QUERY_RESULT EXEC_QUERY(String ANS_SQL, String TEST_SQL, String DockerName, boolean Ordered, int DATABASE) throws IOException, JSchException {
        String CMD = QUERY_SQL[DATABASE];
        CMD = Ordered ? CMD.replaceAll("#CONFIG#", QUERY_CONFIG[DATABASE]) : CMD.replaceAll("#CONFIG#", "");
        CMD = CMD.replaceAll("#DockerNAME#", DockerName).replaceAll("#ANS_SQL#", ANS_SQL).replaceAll("#TEST_SQL#", TEST_SQL);
        Remote.Log logs = Remote.EXEC_CMD(new String[]{CMD}).get(0);
        int score = (logs.OUT.length() >= 21 && logs.OUT.charAt(RESULT_INDEX[DATABASE]) == '0') ? 100 : 0;
        return new QUERY_RESULT(score, logs.exec_time, logs.OUT, logs.ERROR);
    }

    public static QUERY_RESULT EXEC_TRIGGER(String ANS_TABLE_PATH, String TEST_SQL, String TEST_DATA_PATH, int TEST_CONFIG, String DockerName, int DATABASE, String TARGET_TABLE) throws IOException, JSchException {
        TEST_SQL = TEST_SQL.replaceAll("\\$\\$", "####");
        String[] CMD = TRIGGER_SQL[DATABASE];
        for (int i = 0; i < CMD.length; i++)
            CMD[i] = CMD[i]
                    .replaceAll("#DockerNAME#", DockerName)
                    .replaceAll("#TEST_DATA_PATH#", TEST_DATA_PATH)
                    .replaceAll("#ANS_TABLE_PATH#", ANS_TABLE_PATH)
                    .replaceAll("#TARGET_TABLE#", TARGET_TABLE)
                    .replaceAll("#TEST_CONFIG#", String.valueOf(Math.max(TEST_CONFIG, 1)))
                    .replaceAll("#TEST_SQL#", TEST_SQL)
                    .replaceAll("####", " \\\\\\$\\$ ");
        ;
        Remote.Log logs = Remote.EXEC_CMD(CMD).get(CMD.length - 1);
        int score = logs.OUT.length() > 22 ? Integer.parseInt(logs.OUT.substring(16, 22).replaceAll(" ", "")) : 0;
        score = Math.max(score, 0);
        return new QUERY_RESULT(score, logs.exec_time, logs.OUT, logs.ERROR);
    }
}