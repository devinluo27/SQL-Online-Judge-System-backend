//package ooad.demo.config;
//
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
///***
// * 四、用户登录认证逻辑：UserDetailsService
// * 1、创建自定义UserDetailsService
// *     这是实现自定义用户认证的核心逻辑，
// *     loadUserByUsername(String username)的参数就是登录时提交的用户名，
// *     返回类型是一个叫UserDetails的接口，需要在这里构造出他的一个实现类User，
// *     这是Spring security提供的用户信息实体。
// */
//
//public class UserDetailsServiceImpl  implements UserDetailsService {
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        //需要构造出 org.springframework.security.core.userdetails.User 对象并返回
//
//
//        return null;
//    }
//}