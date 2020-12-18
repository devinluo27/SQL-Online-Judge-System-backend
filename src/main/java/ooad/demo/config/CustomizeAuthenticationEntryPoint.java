package ooad.demo.config;

import com.alibaba.fastjson.JSON;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomizeAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e)
            throws IOException, ServletException {
        JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
        httpServletResponse.setContentType("text/json;charset=utf-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(result));
    }

}