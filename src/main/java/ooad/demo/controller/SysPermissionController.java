package ooad.demo.controller;

import lombok.extern.slf4j.Slf4j;
import ooad.demo.mapper.SysPermissionMapper;
import ooad.demo.utils.ResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class SysPermissionController {

    @Autowired
    SysPermissionMapper sysPermissionMapper;

    // TODO: NEW URL
    @GetMapping("/admin/addPermissionToRole")
    public void addPermissionToRole(@RequestParam(value = "role") String role_code,
                                    @RequestParam(value = "permission_code") String permission_code,
                                    HttpServletResponse response){
        if (sysPermissionMapper.addPermission2Role(role_code,permission_code) != 1){
            ResultTool.writeResponseFail(response);
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    // TODO: NEW URL
    @GetMapping("/admin/deletePermissionOfRole")
    public void deletePermissionOfRole(@RequestParam(value = "role") String role_code,
                                       @RequestParam(value = "permission_code") String permission_code,
                                       HttpServletResponse response){
        if (sysPermissionMapper.deletePermission4Role(role_code,permission_code) != 1){
            ResultTool.writeResponseFail(response);
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    // TODO: NEW URL  有锅！！！！
    @GetMapping("/admin/addUserToNewRole")
    public void addUserToNewRole(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "role_code") String role_code,
                                 HttpServletResponse response){
        try{
            sysPermissionMapper.addNewRoleToUser(sid, role_code);
            ResultTool.writeResponseSuccess(response);
        }catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFail(response);
        }
    }

    // TODO: NEW URL
    @GetMapping("/admin/deleteUserRole")
    public void deleteUserRole(@RequestParam(value = "sid") Integer sid,
                                 @RequestParam(value = "role_code") String role_code,
                                 HttpServletResponse response){
        try{
            sysPermissionMapper.deleteUserRole(sid, role_code);
            ResultTool.writeResponseSuccess(response);
        }catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFail(response);
        }
    }


}
