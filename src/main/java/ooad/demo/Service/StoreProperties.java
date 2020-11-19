package ooad.demo.Service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 知识点讲解:
 * 1. @ConfigurationProperties 注解
 *    注有该注解的类, 类中的变量可以通过外接的.properties文件进行配置, 比如下面这个例子, 由于我在application.properties文件中声明了"storage.location=upload", 在Spring运行过程中, location的值会被配置为"upload", 这也是一种依赖注入的方式.
 */
@ConfigurationProperties("store")
@EnableConfigurationProperties(StoreProperties.class)
public class StoreProperties {
    private String location;

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}