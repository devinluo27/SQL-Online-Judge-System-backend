package ooad.demo.mapper;

import ooad.demo.pojo.Database;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface DataBaseMapper {
    Database selectDatabaseById(Integer database_id);
    int addDatabase(Database database);
}
