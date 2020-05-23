package com.tomorrow.wechat_pay_score.util.exception;

import com.tomorrow.wechat_pay_score.util.CommonResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;


/**
 * 异常统一处理
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * post请求参数校验抛出的异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        //获取异常中随机一个异常信息
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return CommonResult.fail(409, defaultMessage);
    }

    /**
     * get请求参数校验抛出的异常
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public CommonResult bindExceptionHandler(BindException e){
        //获取异常中随机一个异常信息
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return CommonResult.fail(409, defaultMessage);
    }


    /**
     * 处理自定义的业务异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = SpringExceptionResolver.class)
    @ResponseBody
    public CommonResult bizExceptionHandler(HttpServletRequest req, SpringExceptionResolver e) {
        return CommonResult.fail(Integer.parseInt(e.getErrorCode()), e.getErrorMsg());
    }

    /**
     * 处理其他异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResult exceptionHandler(HttpServletRequest req, Exception e) {
        return CommonResult.fail(503, "系统内部错误!");
    }

    /**
     * 处理请求错误异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public CommonResult httpRequestMethodNotSupportedExceptionHandler(HttpServletRequest req, Exception e) {
        return CommonResult.fail(500, "请求方式错误!");
    }
}
