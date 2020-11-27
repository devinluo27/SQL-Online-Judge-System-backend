package ooad.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将所有/static/** 访问都映射到classpath:/static/ 目录下
        if(!registry.hasMappingForPattern("/files_for_students/**")){
            registry.addResourceHandler("/files_for_students/**").addResourceLocations("classpath:/static/files_for_students/");
        }
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {


//        registry.addResourceHandler("/**")
//                .addResourceLocations("file:/Users/nongnong/Desktop/");
//
//        registry.addResourceHandler("/static/**")
//                .addResourceLocations("/WEB-INF/view/react/build/static/");
//        registry.addResourceHandler("/*.js")
//                .addResourceLocations("/WEB-INF/view/react/build/");
//        registry.addResourceHandler("/*.json")
//                .addResourceLocations("/WEB-INF/view/react/build/");
//        registry.addResourceHandler("/*.ico")
//                .addResourceLocations("/WEB-INF/view/react/build/");
//        registry.addResourceHandler("/index.html")
//                .addResourceLocations("/Users/nongnong/IdeaProjects/OOAD_1/src/main/webapp/WEB-INF/view/react/public/index.html");
//    }

}