package ooad.demo;

import ooad.demo.mapper.RecordMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    DataSource dataSource;

    @Autowired
    RecordMapper recordMapper;

    @Test
    void contextLoads() throws SQLException {
        System.out.print(dataSource.getClass());

        //获得数据库连接
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    public void judge() {
        String standard = "select * from record";
        String code = "elect * from record";
        List<LinkedHashMap<String, Object>>  a;
        try {
            a = recordMapper.judge(standard, code);
        }
        catch (DataAccessException e){

        }
//        System.out.println(a.size() == 0);
    }

    @Test
    public void getPublicData(){
        String sql = "";
        List<LinkedHashMap<String, Object>> list = recordMapper.runSql(sql);
    }

}
