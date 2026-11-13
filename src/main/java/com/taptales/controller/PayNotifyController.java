package com.taptales.controller;

import com.taptales.entity.PromoOrder;
import com.taptales.service.PromoOrderService;
import com.taptales.service.WxPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PayNotifyController {

    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private PromoOrderService orderService;

    /**
     * WeChat Pay async notification.
     * Called by WeChat when a payment completes.
     */
    @PostMapping("/notify")
    public Map<String, String> notify(
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestBody String body) {

        try {
            Map<String, Object> tx = wxPayService.parseNotify(body, timestamp, nonce, signature, serial);
            if (tx == null) {
                return failMap("parse failed");
            }

            String outTradeNo = String.valueOf(tx.getOrDefault("out_trade_no", ""));
            String tradeState = String.valueOf(tx.getOrDefault("trade_state", ""));

            if ("SUCCESS".equals(tradeState)) {
                PromoOrder order = orderService.getOrderByNo(outTradeNo);
                if (order != null && order.getStatus() == 0) {
                    orderService.payOrder(order.getId());
                }
                return okMap();
            }
            // CLOSED / PAYERROR / REFUND — no action needed
            return okMap();
        } catch (Exception e) {
            e.printStackTrace();
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
        r.put("message", msg);
        return r;
    }
}
