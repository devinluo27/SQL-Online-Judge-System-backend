//package ooad.demo.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import javax.sql.DataSource;
//
//
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        //首页都可以访问，功能页需要权限
//        http.authorizeRequests().
//                antMatchers("/**").permitAll()
////                .antMatchers("/user/**").permitAll()
////                .antMatchers("/user/**").hasRole("student")
//                .antMatchers("/admin/**").hasRole("admin");
////        super.configure(http);
//
//        // 没有权限自动登录页
//        http.formLogin()
//                .loginPage("/index.html")
//                .loginProcessingUrl("/perform_login")
//                .defaultSuccessUrl("/homepage.html",true)
//                .usernameParameter("username")
//                .passwordParameter("password");
//
//        //防止网站工具： get post
//        http.csrf().disable();
//        http.logout()
//                .logoutSuccessUrl("/")
//                .logoutUrl("logout");
//
//        //cookies
////        http.rememberMe().rememberMeParameter("remember");
//
//    }
//
//    //认证
//    //密码加密：passwordencoder
//    //硬编码
//    @Autowired
//    private DataSource dataSource;
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
//                .withUser("asd").password(new BCryptPasswordEncoder().encode("123456")).roles("admin")
//                .and()
//                .withUser("root").password("123").roles("student");
//
//        auth.jdbcAuthentication()
//                .dataSource(dataSource)
//                .usersByUsernameQuery("select sid as username, user_password as password, enabled from user_db where sid = cast(? as int)")
//                .authoritiesByUsernameQuery("select user_id as username, authority from authorities where user_id = cast(? as int)")
//                .passwordEncoder(new BCryptPasswordEncoder());
//    }
//
//    @Override
//    protected UserDetailsService userDetailsService() {
//        return super.userDetailsService();
//    }
//}
//
//
//
