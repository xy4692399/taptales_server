package com.taptales.controller;

import com.taptales.entity.PromoOrder;
import com.taptales.entity.Redemption;
import com.taptales.service.PromoOrderService;
import com.taptales.service.RedemptionService;
import com.taptales.service.UserService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/order")
public class PromoOrderController {
    @Autowired
    private PromoOrderService orderService;
    @Autowired
    private RedemptionService redemptionService;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody Map<String, Long> params) {
        Long userId = com.taptales.util.UserContext.getUserId();
        Long activityId = params.get("activityId");
        try {
            PromoOrder order = orderService.createOrder(userId, activityId);
            return Result.ok(order);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable Long orderId) {
        try {
            boolean success = orderService.payOrder(orderId);
            if (success) return Result.ok();
            return Result.fail("payment failed");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result myOrders() {
        Long userId = com.taptales.util.UserContext.getUserId();
        List<PromoOrder> orders = orderService.getUserOrders(userId);
        return Result.ok(orders);
    }

    @GetMapping("/detail/{orderId}")
    public Result orderDetail(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderWithDetails(orderId));
    }

    @GetMapping("/qr/{orderId}")
    public Result getQrData(@PathVariable Long orderId) {
        Long userId = com.taptales.util.UserContext.getUserId();
        PromoOrder order = orderService.getById(orderId);
        if (order == null) return Result.fail("order not found");
        if (!order.getUserId().equals(userId)) return Result.fail("not your order");
        if (order.getStatus() != 1) return Result.fail("order not paid");

        List<Redemption> redemptions = redemptionService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Redemption>()
                        .eq(Redemption::getOrderId, orderId));
        int alreadyRedeemed = redemptions.stream().mapToInt(r -> r.getCount()).sum();
        int remaining = order.getTotalCount() - alreadyRedeemed;

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("remaining", remaining);
        data.put("totalCount", order.getTotalCount());
        data.put("shopId", order.getShopId());
        return Result.ok(data);
    }

    @PostMapping("/redeem")
    public Result redeem(@RequestBody Map<String, Object> params) {
        Long orderId = ((Number) params.get("orderId")).longValue();
        Long userId = ((Number) params.get("userId")).longValue();
        Long drinkId = params.get("drinkId") != null ? ((Number) params.get("drinkId")).longValue() : null;
        Integer count = ((Number) params.get("count")).intValue();

        try {
            Redemption redemption = redemptionService.redeem(orderId, userId, drinkId, count);
            return Result.ok(redemption);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/byOrderNo/{orderNo}")
    public Result getByOrderNo(@PathVariable String orderNo) {
        PromoOrder order = orderService.getOrderByNo(orderNo);
        if (order == null) return Result.fail("order not found");
        List<Redemption> redemptions = redemptionService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Redemption>()
                        .eq(Redemption::getOrderId, order.getId()));
        int alreadyRedeemed = redemptions.stream().mapToInt(r -> r.getCount()).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("remaining", order.getTotalCount() - alreadyRedeemed);
        result.put("user", userService.getById(order.getUserId()));
        return Result.ok(result);
    }
}