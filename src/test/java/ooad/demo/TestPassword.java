package ooad.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestPassword {
    public static void getpwd(String pwd){
        System.out.println(new BCryptPasswordEncoder().encode(pwd));
    }
    public static void main(String[] args) throws FileNotFoundException {
//        getpwd("11813221");
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "static/files";
        System.out.println(realPath);
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateDirPath = realPath + "/" + dateFormat + "/";
        File dateDir = new File(dateDirPath);
        if (!dateDir.exists()) dateDir.mkdirs();


    }
}
