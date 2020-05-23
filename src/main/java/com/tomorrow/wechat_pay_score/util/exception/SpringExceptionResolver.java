package com.tomorrow.wechat_pay_score.util.exception;

import lombok.Data;

/**
 * 自定义异常，已知可能出现的异常进行捕获
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
@Data
public class SpringExceptionResolver extends RuntimeException {
    /**
     * 错误码
     */
    protected String errorCode;
    /**
     * 错误信息
     */
    protected String errorMsg;

    public SpringExceptionResolver() {
        super();
    }

    public SpringExceptionResolver(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public SpringExceptionResolver(String errorCode, String errorMsg) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public SpringExceptionResolver(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

}
