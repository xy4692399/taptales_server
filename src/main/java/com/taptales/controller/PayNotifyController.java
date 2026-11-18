package com.taptales.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taptales.config.WxPayConfig;
import com.taptales.entity.PromoOrder;
import com.taptales.service.PromoOrderService;
import com.taptales.util.WXPayUtility;
import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PayNotifyController {

    private static final Logger log = LoggerFactory.getLogger(PayNotifyController.class);

    @Autowired
    private PromoOrderService orderService;
    @Autowired
    private WxPayConfig wxPayConfig;

    /**
     * WeChat Pay v3 async notification handler.
     * Flow per official docs:
     *   1. Print raw headers + body for debugging
     *   2. Verify WeChat signature
     *   3. Decrypt resource payload (AES-256-GCM with APIv3 key)
     *   4. Update order status based on trade_state
     */
    @PostMapping("/notify")
    public Map<String, String> notify(
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestBody String body) {

        // ── 1. Print raw notification info ──────────────────────────────
        log.info("========== [WX-NOTIFY] RAW REQUEST ==========");
        log.info("[WX-NOTIFY] signature={}", signature != null ? signature.substring(0, Math.min(signature.length(), 40)) + "..." : "null");
        log.info("[WX-NOTIFY] timestamp={}", timestamp);
        log.info("[WX-NOTIFY] nonce={}", nonce);
        log.info("[WX-NOTIFY] serial={}", serial);
        log.info("[WX-NOTIFY] body length={} body={}", body == null ? 0 : body.length(), body);
        log.info("==============================================");

        // Build Headers object expected by WXPayUtility
        Headers.Builder hb = new Headers.Builder();
        if (signature  != null) hb.add("Wechatpay-Signature", signature);
        if (timestamp  != null) hb.add("Wechatpay-Timestamp", timestamp);
        if (nonce      != null) hb.add("Wechatpay-Nonce", nonce);
        if (serial     != null) hb.add("Wechatpay-Serial", serial);
        Headers headers = hb.build();

        try {
            // ── 2. Verify signature + decrypt resource ────────────────────
            // Throws IllegalArgumentException on signature mismatch or bad ciphertext
            WXPayUtility.Notification notification = WXPayUtility.parseNotification(
                    wxPayConfig.getApiV3Key(),
                    wxPayConfig.getPublicKeyId(),
                    WXPayUtility.loadPublicKeyFromPath(wxPayConfig.getPublicKeyPath()),
                    headers,
                    body);

            // ── 3. Parse decrypted plaintext ──────────────────────────────
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Map<String, Object> tx = gson.fromJson(notification.getPlaintext(), Map.class);
            log.info("[WX-NOTIFY] event_type={} notify_id={}",
                    notification.getEventType(), notification.getId());
            log.info("[WX-NOTIFY] decrypted transaction={}", tx);

            String outTradeNo = String.valueOf(tx.getOrDefault("out_trade_no", ""));
            String tradeState = String.valueOf(tx.getOrDefault("trade_state", ""));
            String appId       = String.valueOf(tx.getOrDefault("appid", ""));
            String mchId       = String.valueOf(tx.getOrDefault("mchid", ""));
            String transactionId = String.valueOf(tx.getOrDefault("transaction_id", ""));
            String tradeType   = String.valueOf(tx.getOrDefault("trade_type", ""));

            log.info("[WX-NOTIFY] out_trade_no={} trade_state={} appid={} mchid={} transaction_id={} trade_type={}",
                    outTradeNo, tradeState, appId, mchId, transactionId, tradeType);

            // ── 4. Handle payment result ──────────────────────────────────
            if ("SUCCESS".equals(tradeState)) {
                PromoOrder order = orderService.getOrderByNo(outTradeNo);
                if (order == null) {
                    log.warn("[WX-NOTIFY] order not found in DB, out_trade_no={}", outTradeNo);
                } else if (order.getStatus() == 0) {
                    orderService.payOrder(order.getId());
                    log.info("[WX-NOTIFY] ✅ order {} marked as PAID (from status=0)", outTradeNo);
                } else {
                    log.info("[WX-NOTIFY] order {} already paid (status={}), skipping", outTradeNo, order.getStatus());
                }
            } else {
                log.info("[WX-NOTIFY] trade_state={} for order {}, no payment action needed", tradeState, outTradeNo);
            }

            // Acknowledge receipt — WeChat will not retry
            return okMap();

        } catch (IllegalArgumentException e) {
            // Signature verification failed or decrypt error
            log.error("[WX-NOTIFY] ❌ VALIDATION FAILED: {}", e.getMessage());
            return failMap(e.getMessage());
        } catch (Exception e) {
            log.error("[WX-NOTIFY] ❌ UNEXPECTED ERROR: {}", e.getMessage(), e);
            return failMap(e.getMessage());
        }
    }

    private Map<String, String> okMap() {
        Map<String, String> r = new HashMap<>();
        r.put("code", "SUCCESS");
        r.put("message", "成功");
        return r;
    }

    private Map<String, String> failMap(String msg) {
        Map<String, String> r = new HashMap<>();
        r.put("code", "FAIL");
        r.put("message", msg != null ? msg : "未知错误");
        return r;
    }
}