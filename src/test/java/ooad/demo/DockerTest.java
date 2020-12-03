package ooad.demo;

import com.jcraft.jsch.JSchException;
import ooad.demo.judge.DockerPool;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class DockerTest {
    public static void main(String[] args) throws JSchException, IOException {

        String test_sql = "create or replace function ID_check()\n" +
                "    returns trigger\n" +
                "as\n" +
                "$$\n" +
                "declare\n" +
                "    ID            people.id%type;\n" +
                "    W             int= 1;\n" +
                "    i             int = 12;\n" +
                "    sum           int = 5;\n" +
                "    key           int;\n" +
                "    province_id   int;\n" +
                "    province_code varchar;\n" +
                "    province_name varchar;\n" +
                "    city_id       int;\n" +
                "    city_code     varchar;\n" +
                "    city_name     varchar;\n" +
                "    district_code varchar;\n" +
                "    district_name varchar;\n" +
                "    birth_date    date;\n" +
                "    birth_char    varchar;\n" +
                "    date_limit    date = '1900/01/01';\n" +
                "begin\n" +
                "    ID := new.id;\n" +
                "    province_id := cast(substring(ID, 1, 2) as int);\n" +
                "    province_id = 10000 * province_id;\n" +
                "    province_code = cast(province_id as varchar);\n" +
                "    city_id := cast(substring(ID, 1, 4) as int);\n" +
                "    city_id = 100 * city_id;\n" +
                "    city_code = cast(city_id as varchar);\n" +
                "    district_code = substring(ID, 1, 6);\n" +
                "    select name into province_name from district where code = province_code;\n" +
                "    if city_code <> province_code then\n" +
                "    select name into city_name from district where code = city_code;\n" +
                "    end if;\n" +
                "    if(district_code <> city_code and district_code <> province_code) then\n" +
                "    select name into district_name from district where code = district_code;\n" +
                "    end if;\n" +
                "    birth_date = substring(ID, 7, 8):: date;\n" +
                "    birth_char = substring(ID, 7, 8);\n" +
                "    while i > 0\n" +
                "        loop\n" +
                "            W = mod(7 * W, 11);\n" +
                "            sum = sum + W * cast(substring(ID, i, 1) as int);\n" +
                "            i = i - 1;\n" +
                "        end loop;\n" +
                "    key = mod(12 - mod(sum, 11), 11);\n" +
                "    if key < 10 and key <> cast(substring(ID, 18, 1) as int) then\n" +
                "        raise exception 'Invalid ID number';\n" +
                "    elseif key = 10 and 'X' <> substring(ID, 18, 1) then\n" +
                "        raise exception 'Invalid ID number';\n" +
                "    elseif birth_date < date_limit then\n" +
                "        raise exception 'Invalid ID number';\n" +
                "    elseif province_name is null then\n" +
                "        raise exception 'Invalid ID number';\n" +
                "    end if;\n" +
                "    new.id = ID;\n" +
                "    new.birthday = birth_char;\n" +
                "    new.address = coalesce(province_name, '') || coalesce(','||city_name, '') || coalesce(','||district_name, '');\n" +
                "    return new;\n" +
                "end\n" +
                "$$ language plpgsql;"+
                "create trigger ID_trigger\n" +
                "    before insert\n" +
                "    on people\n" +
                "    for each row\n" +
                "execute procedure ID_check();";
        String ans_table_path = "/data/xiangjiahong/project/DBOJ/DockerTest/Trigger_data/answer.sql";
        String test_data_path = "/data/xiangjiahong/project/DBOJ/DockerTest/Trigger_data/testData.sql";
        String test = "select distinct r.title, r.country, r.year_released\n" +
                "from movies m\n" +
                "         join movies r on r.title = m.title and r.year_released > m.year_released order by r.runtime";
        String ans = "select distinct r.title, r.country, r.year_released\n" +
                "from movies m\n" +
                "         join movies r on r.title = m.title and r.year_released > m.year_released ";
        Random r = new Random();
        long time = System.currentTimeMillis();
//        EXEC_TRIGGER(String ANS_TABLE_PATH, String TEST_SQL, String TEST_DATA_PATH, String DockerName, int DATABASE, String TARGET_TABLE) throws IOException, JSchException {
//            Judge.QUERY_RESULT rst = Judge.EXEC_TRIGGER(ans_table_path,test_sql,test_data_path,50,"DBOJ_postgres_db4_-1486911486_1",0,"people");
//        System.out.println(Judge.EXEC_QUERY(ans,test,"test",true,1).score);
//        System.out.println(rst.score);
        DockerPool dockerPool = new DockerPool(5,r.nextInt(),0,"db4",
                "/data/xiangjiahong/project/DBOJ/DockerTest/Trigger_data/db4.sql");
//        for (String str : dockerPool.runningList)
//            System.out.println(str);
//        System.out.println(System.currentTimeMillis() - time);
    }
}














