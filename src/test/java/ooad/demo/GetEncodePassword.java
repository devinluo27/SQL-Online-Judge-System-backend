package ooad.demo;


import ooad.demo.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetEncodePassword {
    public static void getpwd(String pwd){
        System.out.println(new BCryptPasswordEncoder().encode(pwd));
    }
    public static void main(String[] args) throws FileNotFoundException {
        getpwd("11813221");
        String a = "aa";
        int b = Integer.parseInt(a);
    }
}
