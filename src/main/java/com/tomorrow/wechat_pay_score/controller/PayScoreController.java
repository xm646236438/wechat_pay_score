package com.tomorrow.wechat_pay_score.controller;

import com.tomorrow.wechat_pay_score.service.PayScoreService;
import com.tomorrow.wechat_pay_score.util.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tomorrow
 * @date 2020/5/23 0:59
 */
@RestController
@RequestMapping("/pay")
public class PayScoreController {
    @Autowired
    private PayScoreService payScoreService;

    /**
     * 创建支付分订单
     *
     * @param orderNo       订单号, 只是测试demo, 所以你懂的
     * @param depositAmount 金额, 只是测试demo, 所以你懂的
     * @return
     */
    @RequestMapping(value = "/score", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult wakeUpPaymentPoints(
            @RequestParam(value = "order_no", required = false) String orderNo,
            @RequestParam(value = "deposit_amount", required = false) int depositAmount
    ) {
        return payScoreService.wakeUpPaymentPoints(orderNo, depositAmount);
    }
}