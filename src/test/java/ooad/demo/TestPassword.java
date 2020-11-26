package ooad.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void getpwd(String pwd){
        System.out.println(new BCryptPasswordEncoder().encode(pwd));
    }
    public static void main(String[] args) {
        getpwd("11813221");
    }
}
