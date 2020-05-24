package com.tomorrow.wechat_pay_score.util.http;

import java.util.Map;

/**
 * @author Tomorrow
 * @date 2020/4/11 11:06
 */
public class HttpUrlUtil {

    /**
     * HTTP请求通过 & 拼接, 返回 URL
     * 请求地址 key: url
     * @param map
     * @return
     */
    public static String urlJoint(Map<String, Object> map) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if ("url".equals(entry.getKey())) {
                stringBuffer.insert(0, entry.getValue() + "?");
                continue;
            }
            stringBuffer.append(entry.getKey() + "=" + entry.getValue());
            stringBuffer.append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }
}
