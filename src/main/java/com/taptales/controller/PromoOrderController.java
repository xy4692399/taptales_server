package com.taptales.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.taptales.entity.PromoOrder;
import com.taptales.entity.Redemption;
import com.taptales.entity.User;
import com.taptales.service.PromoOrderService;
import com.taptales.service.RedemptionService;
import com.taptales.service.UserService;
import com.taptales.service.WxPayService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Autowired
    private WxPayService wxPayService;

    @Value("${file.upload-path}")
    private String uploadPath;

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

    @GetMapping("/prepay/{orderId}")
    public Result prepay(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = com.taptales.util.UserContext.getUserId();
        PromoOrder order = orderService.getById(orderId);
        if (order == null) return Result.fail("order not found");
        if (!order.getUserId().equals(userId)) return Result.fail("not your order");
        if (order.getStatus() != 0) return Result.fail("order already paid");
        try {
            User user = userService.getById(order.getUserId());
            if (user == null || user.getOpenid() == null) return Result.fail("wechat openid not found");
            Map<String, Object> payParams = wxPayService.createJsapiPrepay(
                    order.getId(), order.getOrderNo(), order.getTotalPrice(), user.getOpenid(), request);
            return Result.ok(payParams);
        } catch (Exception e) {
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
        List<PromoOrder> orders = orderService.getUserOrdersAll(userId);
        return Result.ok(orders);
    }

    @GetMapping("/detail/{orderId}")
    public Result orderDetail(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderWithDetails(orderId));
    }

    @GetMapping("/qr/{orderId}")
    public Result getQrData(@PathVariable Long orderId, HttpServletRequest request) {
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

        String qrUrl = generateQrImage(order.getOrderNo(), request);

        Map<String, Object> data = new HashMap<>();
        data.put("qrUrl", qrUrl);
        data.put("orderNo", order.getOrderNo());
        data.put("remaining", remaining);
        data.put("totalCount", order.getTotalCount());
        data.put("shopId", order.getShopId());
        return Result.ok(data);
    }

    /**
     * Generate a QR code PNG image for the given content string.
     * Saves to uploads directory and returns the accessible URL.
     */
    private String generateQrImage(String content, HttpServletRequest request) {
        try {
            int size = 400;
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();
            String filename = "qr_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
            Path filePath = Paths.get(dir.getAbsolutePath(), filename);
            java.io.OutputStream out = new java.io.FileOutputStream(filePath.toFile());
            javax.imageio.ImageIO.write(img, "png", out);
            out.close();

            String scheme = request.getScheme();
            String host = request.getServerName() + ":" + request.getServerPort();
            return scheme + "://" + host + "/uploads/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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