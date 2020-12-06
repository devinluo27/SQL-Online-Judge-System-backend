package ooad.demo.config;

import cn.shuibo.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomerUserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        //获取用户信息
        UserDetails user = userDetailsService.loadUserByUsername(username);

        //比较前端传入的密码和数据库中加密的密码是否相等
//        String plainText = RSAUtil.decrypt(password);
        return null;

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }
}
