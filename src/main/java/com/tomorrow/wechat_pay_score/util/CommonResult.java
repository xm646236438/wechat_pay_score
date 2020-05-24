package com.tomorrow.wechat_pay_score.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * json返回的数据封装
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
@Getter
@Setter
@ToString
public class CommonResult {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;

    public CommonResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public CommonResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonResult(int code) {
        this.code = code;
    }

    /**
     * 成功的时候，返回 message 和 object
     * @param message
     * @param object
     * @return
     */
    public static CommonResult success(String message, Object object) {
        return new CommonResult(200, message, object);
    }

    /**
     * 成功的时候，就返回 message 说明
     * @param message
     * @return
     */
    public static CommonResult success(String message) {
        return new CommonResult(200, message);
    }

    /**
     * 成功的时候，不需要返回任何数据
     * @return
     */
    public static CommonResult success() {
        return new CommonResult(200);
    }

    /**
     * 自定义返回状态码
     * @param code
     * @param message
     * @return
     */
    public static CommonResult fail(int code, String message) {
        return new CommonResult(code, message);
    }

    /**
     * 自定义返回状态码
     * @param code
     * @param message
     * @return
     */
    public static CommonResult failMessage(int code, String message, Object object) {
        return new CommonResult(code, message, object);
    }

}
