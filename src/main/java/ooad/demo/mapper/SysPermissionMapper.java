package ooad.demo.mapper;

import ooad.demo.pojo.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysPermissionMapper {
    List<SysPermission> selectPermissionListByUser(int sid);
    List<SysPermission> selectListByPath(String requestUrl);


}

