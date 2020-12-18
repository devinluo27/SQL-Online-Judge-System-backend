package ooad.demo.config;

import com.alibaba.fastjson.JSON;
import ooad.demo.mapper.UserMapper;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @Author: Yunhao Luo
 * @Description: 登录成功处理逻辑
 * @Date
 */
@Component
public class CustomizeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException, ServletException {

        // 更新用户表上次登录时间、更新人、更新时间等字段
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer sid = Integer.parseInt(userDetails.getUsername());
        userMapper.setLastLoginTime(sid);

//        userDB.setLastLoginTime(new Date());
//        userDB.setUpdateTime(new Date());
//        userDB.setUpdateUser(userDB.getId());
//        userMapper.update(sysUser);

        //此处还可以进行一些处理，比如登录成功之后可能需要返回给前台当前用户有哪些菜单权限，
        //进而前台动态的控制菜单的显示等，具体根据自己的业务需求进行扩展

        //返回json数据
        JsonResult<String> result = ResultTool.success();
        Collection<? extends GrantedAuthority> authorities =  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        int role_count = 0;
        for (GrantedAuthority authority : authorities) {
            if(authority.getAuthority().equals("admin")) {
                role_count++;
            }
            else if (authority.getAuthority().equals("TA")){
                role_count++;
            }
        }
        if (role_count == 1){
            result.setData("TA");
        }
        else if(role_count == 2){
            result.setData("admin");
        }
        else {
            result.setData("user");
        }
        //处理编码方式，防止中文乱码的情况
        httpServletResponse.setContentType("text/json;charset=utf-8");
        //塞到HttpServletResponse中返回给前台
        httpServletResponse.getWriter().write(JSON.toJSONString(result));
    }
}