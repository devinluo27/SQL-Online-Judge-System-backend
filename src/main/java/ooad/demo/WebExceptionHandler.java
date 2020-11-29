package ooad.demo;

import ooad.demo.ErrorHandler.CommonEnum;
import ooad.demo.ErrorHandler.ResultBody;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@ResponseBody
public class WebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);

//
//    @ExceptionHandler(value =NullPointerException.class)
//    @ResponseBody
//    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e){
//        logger.error("发生空指针异常！原因是:", e);
//        return ResultBody.error(CommonEnum.BODY_NOT_MATCH);
//    }
//
//    /**
//     * 处理空指针的异常
//     * @param req
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(value =NullPointerException.class)
//    @ResponseBody
//    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e){
//        logger.error("发生空指针异常！原因是:",e);
//        return ResultBody.error(CommonEnum.BODY_NOT_MATCH);
//    }
//
//
//    /**
//     * 处理其他异常
//     * @param req
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(value =Exception.class)
//    @ResponseBody
//    public ResultBody exceptionHandler(HttpServletRequest req, Exception e){
//        logger.error("未知异常！原因是:",e);
//        return ResultBody.error(CommonEnum.INTERNAL_SERVER_ERROR);
//    }


}
