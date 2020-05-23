package com.tomorrow.wechat_pay_score.util.wechart;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
public class HMACSHA256 {
    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * sha256_HMAC加密
     *
     * @param parameters 参数
     * @param key        秘钥
     * @return 加密后字符串
     */
    public static String sha256_HMAC(SortedMap<Object, Object> parameters, String key) {
        // 对数据进行排序
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : parameters.entrySet()) {
            // 去除掉空参数以及sign
            if (entry.getValue() != null && entry.getKey() != "sign") {
                sb = sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            ;
        }
        // 拼接API密钥
        sb.append("key=" + key);
        // 待签名字符串
        String message = sb.toString();

        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes).toUpperCase();
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }
}
