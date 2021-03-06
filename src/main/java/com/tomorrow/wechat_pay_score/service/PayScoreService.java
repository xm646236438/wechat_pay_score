package com.tomorrow.wechat_pay_score.service;

import com.tomorrow.wechat_pay_score.util.CommonResult;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Tomorrow
 * @date 2020/5/23 0:59
 */
public interface PayScoreService {

    /**
     * 创建支付分订单
     *
     * @param orderNo
     * @param depositAmount
     * @return
     */
    CommonResult wakeUpPaymentPoints(String orderNo, int depositAmount);

    /**
     * 查询支付分订单
     *
     * @param orderNo
     * @return
     */
    CommonResult query(String orderNo);

    /**
     * 完结支付分订单
     *
     * @param orderNo
     * @param amount
     * @return
     */
    CommonResult refund(String orderNo, int amount);

    /**
     * 取消支付分订单
     *
     * @param orderNo
     * @return
     */
    CommonResult cancel(String orderNo);

    /**
     * 小程序免押金回调通知
     *
     * @param request
     * @return
     */
    ResponseEntity payScoreCallbackNotification(HttpServletRequest request);
}
