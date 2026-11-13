package com.taptales.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WxPayConfig {
    @Value("${wechat.app-id}")
    private String appId;
    @Value("${wechat.app-secret}")
    private String appSecret;
    @Value("${wechat.pay.mch-id}")
    private String merchantId;
    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;
    @Value("${wechat.pay.cert-serial-number}")
    private String certSerialNumber;
    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;



    @Value("${wechat.pay.public-key-path}")
    private String publicKeyPath;


    @Value("${wechat.pay.public-key-id}")
    private String publicKeyId;

    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;

    public String getAppId() { return appId; }
    public String getAppSecret() { return appSecret; }
    public String getMerchantId() { return merchantId; }
    public String getApiV3Key() { return apiV3Key; }
    public String getCertSerialNumber() { return certSerialNumber; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public String getNotifyUrl() { return notifyUrl; }
    public String getPublicKeyPath() {
        return publicKeyPath;
    }
    public String getPublicKeyId() {
        return publicKeyId;
    }
}
