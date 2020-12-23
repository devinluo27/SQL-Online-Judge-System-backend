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

    int addPermission2Role(String input_role_code, String input_permission_code);

    int deletePermission4Role(String input_role_code, String input_permission_code);

    int addNewRoleToUser(Integer sid, String role_code);

    int deleteUserRole(Integer sid, String role_code);
}
