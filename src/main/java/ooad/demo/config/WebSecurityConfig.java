package ooad.demo.config;

import ooad.demo.Service.CustomizeAbstractSecurityInterceptor;
import ooad.demo.Service.CustomizeAccessDecisionManager;
import ooad.demo.Service.CustomizeFilterInvocationSecurityMetadataSource;
import ooad.demo.Service.UserDetailsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        //???????????????????????????????????????
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // ?????????????????????????????????hash???????????????
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

        //http?????????????????????????????????????????????????????????????????????
//        super.configure(http);
        http.csrf().disable();
        http.cors().and().authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll();

        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setAccessDecisionManager(accessDecisionManager);//?????????????????????
                        o.setSecurityMetadataSource(securityMetadataSource);//??????????????????
                        return o;
                    }
                })
                .and().exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint) //; //???????????????????????????????????????????????????
                .and()
                .formLogin().permitAll().successHandler(successHandler).failureHandler(failureHandler)
                .and()
                .logout().permitAll().logoutSuccessHandler(logoutSuccessHandler).deleteCookies("JSESSIONID")
                .and().sessionManagement().maximumSessions(3)
                .expiredSessionStrategy(sessionInformationExpiredStrategy); //????????????????????????????????????????????????(??????????????????)

        http.addFilterBefore(securityInterceptor, FilterSecurityInterceptor.class);

//        http.requiresChannel().antMatchers("**").requiresSecure();
//                .and().requiresChannel().anyRequest().requiresInsecure();
//                .and().httpBasic();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // ??????????????????
        auth.userDetailsService(userDetailsService());
    }


}