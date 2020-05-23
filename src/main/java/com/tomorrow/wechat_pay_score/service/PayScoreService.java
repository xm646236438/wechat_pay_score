package com.tomorrow.wechat_pay_score.service;

import com.tomorrow.wechat_pay_score.util.CommonResult;

/**
 * @author Tomorrow
 * @date 2020/5/23 0:59
 */
public interface PayScoreService {

    /**
     * 创建支付分订单
     * @param orderNo
     * @param depositAmount
     * @return
     */
    CommonResult wakeUpPaymentPoints(String orderNo, int depositAmount);
}
