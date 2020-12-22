package ooad.demo.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Hutengfei
 * @Description:
 * @Date Create in 2019/7/22 19:52
 */
@Slf4j
public class ResultTool {

    public static JsonResult success() {
        return new JsonResult(true);
    }

    public static <T> JsonResult<T> success(T data) {
        return new JsonResult(true, data);
    }

    public static JsonResult fail() {
        return new JsonResult(false);
    }

    public static JsonResult fail(ResultCode resultEnum) {
        return new JsonResult(false, resultEnum);
    }

    public static void writeResponseFail(HttpServletResponse response, ResultCode code) {
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.fail(code);
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("Write Response Fails!", e);
            e.printStackTrace();
        }
    }

    public static void writeResponseFail(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.fail();
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("Write Response Fails!", e);
            e.printStackTrace();
        }
    }

    public static void writeResponseFailWithData(HttpServletResponse response, ResultCode code, Object data) {
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.fail(code);
        result.setData(data);
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("Write Response Fails!", e);
            e.printStackTrace();
        }
    }

    public static void writeResponseSuccess(HttpServletResponse response){
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.success();
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("Write Response Fails!", e);
            e.printStackTrace();
        }
    }
    public static void writeResponseSuccessWithData(HttpServletResponse response, Object data){
        response.setContentType("application/json;charset=UTF-8");
        JsonResult result = ResultTool.success();
        result.setData(data);
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("Write Response Fails!", e);
            e.printStackTrace();
        }
    }

}