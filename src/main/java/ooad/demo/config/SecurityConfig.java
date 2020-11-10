package ooad.demo.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //首页都可以访问，功能页需要权限
        http.authorizeRequests().
                antMatchers("/**").permitAll()
                .antMatchers("/add**").hasRole("teacher")
                .antMatchers("/level2/**").hasRole("admin");
//        super.configure(http);
        // 没有权限自动登录页
        http.formLogin();
        http.logout().logoutSuccessUrl("/");
    }

    //认证
    //密码加密：passwordencoder
    //硬编码
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
                .withUser("asd").password(new BCryptPasswordEncoder().encode("123abb")).roles("teacher")
                .and()
                .withUser("root").password("123").roles("student");
    }
}



