package com.taptales.controller;

import com.taptales.entity.PromoOrder;
import com.taptales.entity.Redemption;
import com.taptales.entity.User;
import com.taptales.service.PromoOrderService;
import com.taptales.service.RedemptionService;
import com.taptales.service.UserService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/redemption")
public class RedemptionController {
    @Autowired
    private RedemptionService redemptionService;
    @Autowired
    private PromoOrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping("/shop/{shopId}")
    public Result getShopRedemptions(@PathVariable Long shopId) {
        List<Redemption> list = redemptionService.getShopRedemptions(shopId);
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Redemption r : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("orderId", r.getOrderId());
            item.put("count", r.getCount());
            item.put("redeemedAt", r.getRedeemedAt());
            if (r.getOrderId() != null) {
                PromoOrder order = orderService.getById(r.getOrderId());
                item.put("orderNo", order != null ? order.getOrderNo() : "");
            }
            if (r.getUserId() != null) {
                User user = userService.getById(r.getUserId());
                item.put("nickname", user != null ? user.getNickname() : "unknown");
            }
            enriched.add(item);
        }
        return Result.ok(enriched);
    }
}