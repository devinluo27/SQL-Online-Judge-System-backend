package ooad.demo.Service;

import ooad.demo.mapper.SysPermissionMapper;
import ooad.demo.pojo.SysPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.thymeleaf.util.StringUtils;

import java.util.*;

/**
 * @Author: Yunhao
 * @Description:
 * @Date
 */
@Component
public class CustomizeFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Autowired
    SysPermissionMapper sysPermissionMapper;
    // 返回本次访问所需要的权限
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        //获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        String requestURI = ((FilterInvocation) o).getRequest().getRequestURI();
        // 不带？参数
//        System.out.println("CustomizedMetaSource: " + requestURI);
        // 带？参数
//        System.out.println("CustomizedMetaSource: " + requestUrl);

        //查询具体某个接口的权限
        if(requestUrl.contains("?"))
            requestUrl = StringUtils.substringBefore(requestUrl, "?");


        List<SysPermission> permissionList =  sysPermissionMapper.selectListByPath(requestURI);

//        System.out.println("size of Permission List: " + permissionList.size());
//        System.out.println(requestUrl);
//        for (SysPermission s: permissionList) {
//            System.out.println(s);
//        }

        if(permissionList == null || permissionList.size() == 0){
            // NO NO! OUT OF DATE!请求路径没有配置权限，表明该请求接口可以任意访问
            // TODO: 确保所有mapping都要登陆以后才能访问
//             return SecurityConfig.createList("ROLE_LOGIN");
            return null;
        }
        String[] attributes = new String[permissionList.size()];
        for(int i = 0; i < permissionList.size(); i++){
            // permission code is a English word of a certain description
            attributes[i] = permissionList.get(i).getPermission_code();
        }
        return SecurityConfig.createList(attributes);
    }

    // TODO: Warning to handle!
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {

         return null;

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}