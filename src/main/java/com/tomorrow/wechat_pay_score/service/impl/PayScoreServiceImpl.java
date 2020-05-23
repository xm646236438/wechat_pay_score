package com.tomorrow.wechat_pay_score.service.impl;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.tomorrow.wechat_pay_score.service.PayScoreService;
import com.tomorrow.wechat_pay_score.util.CommonResult;
import com.tomorrow.wechat_pay_score.util.Utils;
import com.tomorrow.wechat_pay_score.util.exception.SpringExceptionResolver;
import com.tomorrow.wechat_pay_score.util.wechart.HMACSHA256;
import com.tomorrow.wechat_pay_score.util.wechart.PayScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
@Service
@Slf4j
public class PayScoreServiceImpl implements PayScoreService {
    @Value("${project.APP_ID}")
    public String appId;
    @Value("${project.MCH_ID}")
    public String mchId;
    @Value("${project.MCH_KEY}")
    public String mchKey;
    @Value("${project.SERVICE_ID}")
    public String serviceId;
    @Value("${project.SERIAL_NO}")
    public String serialNo;

    @Value("${project.NOTIFY_URL}")
    public String notifyURL;
    @Value("${project.CREATE_ORDER_URL}")
    public String createOrderUrl;


    @Override
    public CommonResult wakeUpPaymentPoints(String orderNo, int depositAmount) {
        // 创建支付分订单 请求参数
        JSONObject parameters = new JSONObject();
        parameters.put("out_order_no", orderNo);
        parameters.put("appid", appId);
        parameters.put("service_id", serviceId);
        parameters.put("service_introduction", "阿啵呲嘚");
        JSONObject timeRange = new JSONObject();
        timeRange.put("start_time", "OnAccept");
        parameters.put("time_range", timeRange);
        JSONObject riskFund = new JSONObject();
        riskFund.put("name", "DEPOSIT");
        riskFund.put("amount", depositAmount);
        riskFund.put("description", "阿啵呲嘚");
        parameters.put("risk_fund", riskFund);
        parameters.put("notify_url", notifyURL);
        parameters.put("need_user_confirm", true);

        JSONObject jsonObject;
        try {
            log.info("请求参数", JSONObject.toJSONString(parameters));
            String data = HttpRequest.post(createOrderUrl)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header(Header.ACCEPT, "application/json")
                    // 签名
                    .header("Authorization", "WECHATPAY2-SHA256-RSA2048" + " "
                            + PayScore.getToken("POST", createOrderUrl, JSONObject.toJSONString(parameters), mchId, serialNo, "pem/apiclient_key.pem"))
                    .body(JSONObject.toJSONString(parameters))
                    .execute().body();
            jsonObject = JSONObject.parseObject(data);
            System.out.println("返回参数" + jsonObject);
        }catch (Exception e) {
            throw new SpringExceptionResolver("500", "网络超时!");
        }
        if (!"CREATED".equals(jsonObject.getString("state"))) {
            throw new SpringExceptionResolver("500", jsonObject.getString("message"));
        }

        // 处理返回数据
        SortedMap<Object, Object> result = new TreeMap<Object, Object>();
        result.put("mch_id", mchId);
        result.put("package", jsonObject.getString("package"));
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonce_str", Utils.getRandomString(32));
        result.put("sign_type", "HMAC-SHA256");
        // 签名
        result.put("sign", HMACSHA256.sha256_HMAC(result, mchKey));
        return CommonResult.success("SUCCESS", result);
    }
}
