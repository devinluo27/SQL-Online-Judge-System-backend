package ooad.demo.config;

import ooad.demo.Service.CustomizeAbstractSecurityInterceptor;
import ooad.demo.Service.CustomizeAccessDecisionManager;
import ooad.demo.Service.CustomizeFilterInvocationSecurityMetadataSource;
import ooad.demo.Service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomizeAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    CustomizeAuthenticationSuccessHandler successHandler;

    @Autowired
    CustomizeAuthenticationFailureHandler failureHandler;

    @Autowired
    CustomizeLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    CustomizeSessionInformationExpiredStrategy sessionInformationExpiredStrategy;

    @Autowired
    CustomizeAccessDecisionManager accessDecisionManager;

    @Autowired
    CustomizeFilterInvocationSecurityMetadataSource securityMetadataSource;

    @Autowired
    private CustomizeAbstractSecurityInterceptor securityInterceptor;



    @Bean
    public UserDetailsService userDetailsService() {
        //获取用户账号密码及权限信息
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 设置默认的加密方式（强hash方式加密）
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public CookieSerializer httpSessionIdResolver(){
//        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
//        cookieSerializer.setSameSite(null);
//        return cookieSerializer;
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //http相关的配置，包括登入登出、异常处理、会话管理等
//        super.configure(http);
        http.csrf().disable();
        http.cors().and().authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll();

        http.authorizeRequests()
//                .antMatchers("/user/addRecord").hasAuthority("query_user")
//                .and()
//                .authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setAccessDecisionManager(accessDecisionManager);//访问决策管理器
                        o.setSecurityMetadataSource(securityMetadataSource);//安全元数据源
                        return o;
                    }
                })
                .and().exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint) //; //匿名用户访问无权限资源时的异常处理
                .and()
                .formLogin().permitAll().successHandler(successHandler).failureHandler(failureHandler)
                .and()
                .logout().permitAll().logoutSuccessHandler(logoutSuccessHandler).deleteCookies("JSESSIONID")
                .and().sessionManagement().maximumSessions(3)
                .expiredSessionStrategy(sessionInformationExpiredStrategy);//会话信息过期策略会话信息过期策略(账号被挤下线)

        http.addFilterBefore(securityInterceptor, FilterSecurityInterceptor.class);

//        http.requiresChannel().antMatchers("**").requiresSecure();
//                .and().requiresChannel().anyRequest().requiresInsecure();
//                .and().httpBasic();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 配置认证方式
        auth.userDetailsService(userDetailsService());
    }


}