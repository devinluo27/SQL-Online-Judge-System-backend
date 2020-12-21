package ooad.demo.utils;

import com.alibaba.fastjson.JSON;
import ooad.demo.Service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class MultiAccessInterceptor  extends HandlerInterceptorAdapter {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        System.out.println(request.getHeader("Content-Type"));

        //判断请求是否属于方法的请求
        if(handler instanceof HandlerMethod){

            HandlerMethod hm = (HandlerMethod) handler;

            //获取方法中的注解,看是否有该注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            if(accessLimit == null){
                return true;
            }

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean login = accessLimit.needLogin();

            String key = request.getRequestURI();
            //如果需要登录
            if(login){
                //获取登录的session进行判断
                //.....
                if (request.getUserPrincipal() == null){
                    System.out.println("Inside preHandle!");
                    return false;
                }
                String username = request.getUserPrincipal().getName();
                key = key + "-" +  username;  //这里假设用户是1,项目中是动态获取的userId
            }

            System.out.println(key);

            //从redis中获取用户访问的次数
//            AccessKey ak = AccessKey.withExpire(seconds);
            // set key to retrieve the value in a given hashmap
            String keyInHashMap = "multi-access-map";
            Integer count = (Integer) redisService.getHashValue(key, keyInHashMap);

            System.out.println("count: " + count);
            if(count == null){
                //第一次访问
                redisService.setHashValue(key, keyInHashMap, 1);
                // set key to get the hashmap
                redisService.setKeyLifeTime(key, seconds, TimeUnit.SECONDS);
            }else if(count < maxCount){
                //加1
                redisService.incr(key, keyInHashMap);
            }else{
                //超出访问次数
                render(response, ResultCode.REACHING_ACCESS_LIMIT); //这里的CodeMsg是一个返回参数
                return false;
            }
        }
        return true;

    }

    private void render(HttpServletResponse response, ResultCode code) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.fail(code);
        response.getWriter().write(JSON.toJSONString(result));
    }

}
