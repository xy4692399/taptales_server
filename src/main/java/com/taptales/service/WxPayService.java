package com.taptales.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taptales.config.WxPayConfig;
import com.taptales.util.WXPayUtility;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.wechat.pay.java.core.Config;

import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.SceneInfo;

@Service
public class WxPayService {
    private static final Logger log = LoggerFactory.getLogger(WxPayService.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private WxPayConfig config;

    /** 兼容反向代理获取真实客户端 IP */
    private String getPayerClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int commaIdx = ip.indexOf(',');
            return commaIdx > 0 ? ip.substring(0, commaIdx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 使用 wechatpay-java SDK 的 JsapiServiceExtension.prepayWithRequestPayment() 下单。
     * SDK 自动完成签名、发送 HTTP，并返回 wx.requestPayment 所需的全部字段。
     */
    public Map<String, Object> createJsapiPrepay(Long orderId, String outTradeNo,
                                                  BigDecimal totalAmount, String openid,
                                                  HttpServletRequest request) throws Exception {
        int amountCents = totalAmount.multiply(new BigDecimal("100")).intValue();
        log.info("-----------------------------------");
        log.info(config.getPrivateKeyPath());

        // 构建 SDK 配置（与官方示例一致）
        Config sdkConfig = new RSAPublicKeyConfig.Builder()
                .merchantId(config.getMerchantId())
                .privateKeyFromPath(config.getPrivateKeyPath())
                .publicKeyFromPath(config.getPublicKeyPath())
                .publicKeyId(config.getPublicKeyId())
                .merchantSerialNumber(config.getCertSerialNumber())
                .apiV3Key(config.getApiV3Key())
                .build();

        JsapiServiceExtension service = new JsapiServiceExtension.Builder()
                .config(sdkConfig)
                .signType("RSA")
                .build();

        PrepayRequest prepayReq = new PrepayRequest();
        prepayReq.setAppid(config.getAppId());
        prepayReq.setMchid(config.getMerchantId());
        prepayReq.setDescription("TapTales团购活动");
        prepayReq.setOutTradeNo(outTradeNo);
        prepayReq.setNotifyUrl(config.getNotifyUrl());

        Amount amount = new Amount();
        amount.setTotal( amountCents);
        amount.setCurrency("CNY");
        prepayReq.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        prepayReq.setPayer(payer);

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(getPayerClientIp(request));
        prepayReq.setSceneInfo(sceneInfo);

        log.info("[WX-SDK] orderId={} outTradeNo={} amountCents={} openid={}",
                orderId, outTradeNo, amountCents, openid);

        try {
            PrepayWithRequestPaymentResponse result = service.prepayWithRequestPayment(prepayReq);

            return buildPayParamsMap(result);
        } catch (Exception e) {

            throw new RuntimeException("微信支付失败: " + e.getMessage(), e);
        }
    }

    /** 将 SDK 响应转为前端可使用的 map（appId/timeStamp/nonceStr/package/signType/paySign） */
    private Map<String, Object> buildPayParamsMap(PrepayWithRequestPaymentResponse result) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", config.getAppId());
        params.put("timeStamp", result.getTimeStamp());
        params.put("nonceStr", result.getNonceStr());
        params.put("package",result.getPackageVal());
        params.put("signType", result.getSignType());
        params.put("paySign", result.getPaySign());
        log.info("[WX-PAY-PARAMS] {}", gson.toJson(params));
        return params;
    }

    public Map<String, Object> parseNotify(String body, String timestamp, String nonce,
                                            String signature, String serial) {
        try {
            log.info("[WX-NOTIFY] timestamp={}, nonce={}, signature={}, serial={}", timestamp, nonce, signature, serial);
            log.info("[WX-NOTIFY] body={}", body);
            Map<String, Object> map = gson.fromJson(body, Map.class);
            Object resource = map.get("resource");
            if (resource == null) { log.warn("[WX-NOTIFY] no resource field"); return null; }
            Map<String, Object> resMap = (Map<String, Object>) resource;
            String ciphertext = (String) resMap.get("ciphertext");
            String resNonce = (String) resMap.get("nonce");
            String associatedData = (String) resMap.getOrDefault("associated_data", "");
            byte[] key = config.getApiV3Key().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] iv = Base64.getDecoder().decode(resNonce);
            byte[] ct = Base64.getDecoder().decode(ciphertext);
            byte[] aad = associatedData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String plain = WXPayUtility.aesAeadDecrypt(key, aad, iv, ct);
            log.info("[WX-NOTIFY] decrypted={}", plain);
            Map<String, Object> result = gson.fromJson(plain, Map.class);
            log.info("[WX-NOTIFY] parsed result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[WX-NOTIFY] parse error: {}", e.getMessage(), e);
            return null;
        }
    }
}