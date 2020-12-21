package ooad.demo.mapper;

import ooad.demo.pojo.Database;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface DataBaseMapper {
    Database selectDatabaseById(Integer database_id);
    Integer addDatabase(Database database);
    ArrayList<Database> queryDatabaseList();
    Integer deleteDatabase(Integer database_id);
}
