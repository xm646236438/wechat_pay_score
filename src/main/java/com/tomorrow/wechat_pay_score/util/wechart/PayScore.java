package com.tomorrow.wechat_pay_score.util.wechart;

import com.tomorrow.wechat_pay_score.util.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

/**
 * @author Tomorrow
 * @date 2020/5/23 1:00
 */
@Slf4j
public class PayScore {

    /**
     * 微信支付API v3 签名
     *
     * @param method       请求类型GET、POST
     * @param url          请求地址
     * @param body         请求数据 GET: 传"" POST: json串
     * @param merchantId   商户号
     * @param certSerialNo 证书序列号
     * @param filename     API证书相对路径
     * @return
     * @throws Exception
     */
    public static String getToken(String method, String url, String body, String merchantId, String certSerialNo, String filename) throws Exception {
        String signStr = "";
        HttpUrl httpurl = HttpUrl.parse(url);
        // 随机字符串
        String nonceStr = Utils.getRandomString(32);
        // 时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        if (StringUtils.isEmpty(body)) {
            body = "";
        }
        String message = buildMessage(method, httpurl, timestamp, nonceStr, body);
        String signature = sign(message.getBytes("utf-8"), filename);
        signStr = "mchid=\"" + merchantId
                + "\",nonce_str=\"" + nonceStr
                + "\",timestamp=\"" + timestamp
                + "\",serial_no=\"" + certSerialNo
                + "\",signature=\"" + signature + "\"";
        log.info("Authorization Token：" + signStr);
        return signStr;
    }

    public static String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }


    public static String sign(byte[] message, String filename) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(getPrivateKey(filename));
        sign.update(message);
        return Base64.encodeBase64String(sign.sign());
    }

    /**
     * 获取私钥。
     *
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String filename) throws IOException {
        // 编译后的相对路径
        ClassPathResource classPathResource = new ClassPathResource(filename);
        InputStream inputStream = classPathResource.getInputStream();
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String content = scanner.useDelimiter("\\A").next();
        // 绝对路径
//        String content = new String(Files.readAllBytes(Paths.get("F:\\key\\publicKey.pem")), "utf-8");
        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            log.info("异常：" + e);
            throw new RuntimeException("无效的密钥格式");
        }
    }
}
