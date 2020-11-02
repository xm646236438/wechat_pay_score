package com.tomorrow.wechat_pay_score.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tomorrow.wechat_pay_score.service.PayScoreService;
import com.tomorrow.wechat_pay_score.util.CommonResult;
import com.tomorrow.wechat_pay_score.util.Utils;
import com.tomorrow.wechat_pay_score.util.exception.SpringExceptionResolver;
import com.tomorrow.wechat_pay_score.util.http.HttpUrlUtil;
import com.tomorrow.wechat_pay_score.util.wechart.ApiV3Util;
import com.tomorrow.wechat_pay_score.util.wechart.HMACSHA256;
import com.tomorrow.wechat_pay_score.util.wechart.PayScore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    @Value("${project.MCH_KEY_VVV}")
    public String mchKeyVVV;
    @Value("${project.SERVICE_ID}")
    public String serviceId;
    @Value("${project.SERIAL_NO}")
    public String serialNo;

    @Value("${project.NOTIFY_URL}")
    public String notifyURL;
    @Value("${project.CREATE_ORDER_URL}")
    public String createOrderUrl;
    @Value("${project.CANCEL_ORDER_URL}")
    public String cancelOrderUrl;


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
            log.info("请求参数" + JSONObject.toJSONString(parameters));
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
        } catch (Exception e) {
            throw new SpringExceptionResolver("500", "网络超时!");
        }
        if (!"CREATED".equals(jsonObject.getString("state"))) {
            throw new SpringExceptionResolver("500", jsonObject.getString("message"));
        }

        // 处理返回数据
        SortedMap<Object, Object> result = new TreeMap<Object, Object>();
        result.put("mch_id", mchId);
        result.put("package", jsonObject.getString("package"));
        result.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonce_str", Utils.getRandomString(32));
        result.put("sign_type", "HMAC-SHA256");
        // 签名
        result.put("sign", HMACSHA256.sha256_HMAC(result, mchKey));
        return CommonResult.success("SUCCESS", result);
    }

    @Override
    public CommonResult query(String orderNo) {
        // 参数的顺序要注意, 不然会报错
        Map<String, Object> map = new HashMap<>();
        map.put("url", createOrderUrl);
        map.put("service_id", serviceId);
        map.put("out_order_no", orderNo);
        map.put("appid", appId);
        String urlJoint = HttpUrlUtil.urlJoint(map);

        JSONObject jsonObject;
        try {
            log.info("请求参数:    " + urlJoint);
            String data = HttpRequest.get(urlJoint)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header(Header.ACCEPT, "application/json")
                    .header("Authorization", "WECHATPAY2-SHA256-RSA2048" + " "
                            + PayScore.getToken("GET", urlJoint, "", mchId, serialNo, "pem/apiclient_key.pem"))
                    .body("")
                    .execute().body();
            jsonObject = JSONObject.parseObject(data);
            System.out.println("返回参数:    " + jsonObject);
        } catch (Exception e) {
            throw new SpringExceptionResolver("500", "网络超时!");
        }
        switch (jsonObject.getString("state")) {
            case "CREATED":
                return CommonResult.fail(500, "订单未进行");
            case "DOING":
            case "DONE":
                return CommonResult.success("SUCCESS", jsonObject);
            case "REVOKED":
                return CommonResult.fail(500, "订单已取消");
            case "EXPIRED":
                return CommonResult.fail(500, "订单已失效");
            default:
                return CommonResult.fail(500, "网络异常");
        }
    }

    @Override
    public CommonResult refund(String orderNo, int amount) {
        JSONObject parameters = new JSONObject();
        parameters.put("appid", appId);
        parameters.put("service_id", serviceId);
        List<JSONObject> postPaymentsList = new ArrayList<>();
        JSONObject postPayments = new JSONObject();
        postPayments.put("name", "test");
        postPayments.put("amount", amount);
        postPaymentsList.add(postPayments);
        parameters.put("post_payments", postPaymentsList);
        parameters.put("total_amount", amount);
        JSONObject timeRange = new JSONObject();
        timeRange.put("end_time", DateUtil.format(new Date(), "yyyyMMddHHmmss"));
        parameters.put("time_range", timeRange);
        JSONObject jsonObject;
        createOrderUrl = createOrderUrl + "/" + orderNo + "/complete";
        try {
            log.info("请求参数:    " + parameters);
            String data = HttpRequest.post(createOrderUrl)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header(Header.ACCEPT, "application/json")
                    .header("Authorization", "WECHATPAY2-SHA256-RSA2048" + " "
                            + PayScore.getToken("POST", createOrderUrl, JSONObject.toJSONString(parameters), mchId, serialNo, "pem/apiclient_key.pem"))
                    .body(JSONObject.toJSONString(parameters))
                    .execute().body();
            jsonObject = JSONObject.parseObject(data);
            System.out.println("返回参数:    " + jsonObject);
        } catch (Exception e) {
            throw new SpringExceptionResolver("500", "网络超时!");
        }
        if (!StringUtils.isEmpty(jsonObject.getString("code"))) {
            return CommonResult.fail(500, jsonObject.getString("message"));
        }
        switch (jsonObject.getString("state")) {
            case "CREATED":
                return CommonResult.fail(500, "订单未进行");
            case "DOING":
            case "DONE":
                return CommonResult.success("SUCCESS", jsonObject);
            case "REVOKED":
                return CommonResult.fail(500, "订单已取消");
            case "EXPIRED":
                return CommonResult.fail(500, "订单已失效");
            default:
                return CommonResult.fail(500, "网络异常");
        }
    }

    @Override
    public CommonResult cancel(String orderNo) {
        JSONObject parameters = new JSONObject();
        parameters.put("appid", appId);
        parameters.put("service_id", serviceId);
        parameters.put("reason", "业务流程取消");
        JSONObject jsonObject;
        cancelOrderUrl = cancelOrderUrl + "/" + orderNo + "/cancel";
        try {
            log.info("请求支付分参数:    " + cancelOrderUrl);
            log.info("请求支付分参数:    " + parameters);
            HttpResponse execute = HttpRequest.post(cancelOrderUrl)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header(Header.ACCEPT, "application/json")
                    .header("Authorization", "WECHATPAY2-SHA256-RSA2048" + " "
                            + PayScore.getToken("POST", cancelOrderUrl, JSONObject.toJSONString(parameters), mchId, serialNo, "pem/apiclient_key.pem"))//头信息，多个头信息多次调用此方法即可
                    .body(JSONObject.toJSONString(parameters))
                    .execute();
            String header = execute.header("Request-ID");
            log.info("请求支付分返回参数HEAD标识:    " + header);
            jsonObject = JSONObject.parseObject(execute.body());
            log.info("请求支付分返回参数:    " + jsonObject);
        } catch (Exception e) {
            throw new SpringExceptionResolver("500", "网络超时!");
        }
        if (!StringUtils.isEmpty(jsonObject.getString("code"))) {
            return CommonResult.fail(500, jsonObject.getString("message"));
        }
        return CommonResult.success("SUCCESS", jsonObject);
    }

    @Override
    public ResponseEntity payScoreCallbackNotification(HttpServletRequest request) {
        try {
            ServletInputStream servletInputStream = request.getInputStream();
            int contentLength = request.getContentLength();
            byte[] callBackInBytes = new byte[contentLength];
            servletInputStream.read(callBackInBytes, 0, contentLength);
            String callBackIn = new String(callBackInBytes, "UTF-8");
            // 模拟确认订单回调通知API
//            String callBackIn = "{\"id\":\"123\",\"create_time\":\"2020-11-02T16:31:35+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"PAYSCORE.USER_CONFIRM\",\"summary\":\"微信支付分服务订单用户已确认\",\"resource\":{\"original_type\":\"payscore\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"1111111111==\",\"associated_data\":\"payscore\",\"nonce\":\"12321321\"}}";
            // 模拟支付成功回调通知API
//            String callBackIn = "{\"id\":\"123\",\"create_time\":\"2020-11-02T16:31:35+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"PAYSCORE.USER_PAID\",\"summary\":\"微信支付分服务订单支付成功\",\"resource\":{\"original_type\":\"payscore\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"1111111111==\",\"associated_data\":\"payscore\",\"nonce\":\"12321321\"}}";

            log.info("【微信支付分免密支付回调】：" + callBackIn);


            JSONObject notifyIn = JSONObject.parseObject(callBackIn);
            if (notifyIn == null) {
                log.error("参数不正确，反序列化失败");
                return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
            }

            //解密回调信息
            JSONObject resource = notifyIn.getJSONObject("resource");
            byte[] key = (mchKeyVVV).getBytes("UTF-8");
            ApiV3Util aesUtil = new ApiV3Util(key);
            String decryptToString = aesUtil.decryptToString(resource.getString("associated_data").getBytes("UTF-8"), resource.getString("nonce").getBytes("UTF-8"), resource.getString("ciphertext"));

            if (StringUtils.isEmpty(decryptToString)) {
                return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
            }
            log.info("【支付分支付回调解密结果：】" + decryptToString);


            // 用户确认成功
            if ("PAYSCORE.USER_CONFIRM".equals(notifyIn.get("event_type"))) {
                log.info("用户确认成功");
                // 处理业务逻辑
            }
            // 支付成功
            if ("PAYSCORE.USER_PAID".equals(notifyIn.get("event_type"))) {
                log.info("用户支付成功");
                // 处理业务逻辑
            }
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error("微信支付回调处理异常，" + e.toString());
            return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
        }
    }
}
