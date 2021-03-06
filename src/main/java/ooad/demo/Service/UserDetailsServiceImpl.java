package ooad.demo.Service;

import lombok.extern.slf4j.Slf4j;
import ooad.demo.mapper.SysPermissionMapper;
import ooad.demo.mapper.UserMapper;
import ooad.demo.pojo.SysPermission;
import ooad.demo.pojo.UserDB;
import ooad.demo.utils.AccessLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        if (username == null || "".equals(username)) {
            // TODO 不知道怎么处理异常
//            throw new InternalAuthenticationServiceException("用户不能为空");
            System.out.println("用户不能为空");
            throw new UsernameNotFoundException("用户不能为空");
//            throw new RuntimeException("用户不能为空");
        }

        // TODO: Testing
        log.info("Try to Login username: " + username);
        Integer sid = null;
        try{
            sid = Integer.parseInt(username);
        } catch (Exception e){
            throw new UsernameNotFoundException("用户不存在");
        }
        //根据用户名查询用户
        UserDB userDB = userMapper.selectUserDBBySidAllInfo(sid);
        if (userDB == null) {
            System.out.println("用户不存在");
            throw new UsernameNotFoundException("用户不存在");
        }

        // list of authorities
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        //获取该用户所拥有的权限
        List<SysPermission> sysPermissions = sysPermissionMapper.selectPermissionListByUser(userDB.getSid());
        // 声明用户授权
        // TODO
//        System.out.println(sysPermissions.size());
//        System.out.println(sysPermissions.get(0));

        if (sysPermissions.get(0) != null) {
            sysPermissions.forEach(sysPermission -> {
                // 将 permission code 添加给该用户
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(sysPermission.getPermission_code());
                grantedAuthorities.add(grantedAuthority);
            });
        }

        boolean enable = true;
        boolean  accountNonExpired = true;
        boolean  credentialsNonExpired = true;
        boolean  accountNonLocked = true;
//        System.out.println(userDB.getSid());
//        System.out.println(userDB.getUser_password());

        return new User(String.valueOf(userDB.getSid()), userDB.getUser_password(), userDB.getEnabled() == 1, accountNonExpired, credentialsNonExpired, accountNonLocked, grantedAuthorities);
    }
}

//    String username：用户名
//    String password： 密码
//    boolean enabled： 账号是否可用
//    boolean accountNonExpired：账号是否过期
//    boolean credentialsNonExpired：密码是否过期
//    boolean accountNonLocked：账号是否锁定
//    Collection<? extends GrantedAuthority> authorities)：用户权限列表
