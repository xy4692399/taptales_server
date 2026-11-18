package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.PromoOrder;
import com.taptales.entity.PromoActivity;
import com.taptales.entity.User;
import com.taptales.mapper.PromoOrderMapper;
import com.taptales.service.PromoActivityService;
import com.taptales.service.PromoOrderService;
import com.taptales.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PromoOrderServiceImpl extends ServiceImpl<PromoOrderMapper, PromoOrder> implements PromoOrderService {
    private static final AtomicLong seq = new AtomicLong(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PromoActivityService activityService;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public PromoOrder createOrder(Long userId, Long activityId) {
        PromoActivity activity = activityService.getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new RuntimeException("活动不存在或已下架");
        }
        long paidCount = count(new LambdaQueryWrapper<PromoOrder>()
                .eq(PromoOrder::getActivityId, activityId)
                .eq(PromoOrder::getStatus, 1));
        if (paidCount >= activity.getTotalTickets()) {
            throw new RuntimeException("活动已售罄");
        }
        if (activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("活动已结束");
        }

        String orderNo = "TT" + LocalDateTime.now().format(FMT) + String.format("%06d", seq.incrementAndGet());
        PromoOrder order = new PromoOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setActivityId(activityId);
        order.setShopId(activity.getShopId());
        order.setTotalPrice(activity.getPrice());
        order.setTotalCount(1);
        order.setStatus(0);
        save(order);
        return order;
    }

    @Override
    public PromoOrder getOrderByNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<PromoOrder>().eq(PromoOrder::getOrderNo, orderNo));
    }

    @Override
    public List<PromoOrder> getUserOrders(Long userId) {
        // Only show PAID orders to the user
        return list(new LambdaQueryWrapper<PromoOrder>()
                .eq(PromoOrder::getUserId, userId)
                .eq(PromoOrder::getStatus, 1)
                .orderByDesc(PromoOrder::getCreatedAt));
    }

    @Override
    public List<PromoOrder> getUserOrdersAll(Long userId) {
        // Return ALL orders regardless of status so users can see pending ones
        // and manually sync them after payment
        return list(new LambdaQueryWrapper<PromoOrder>()
                .eq(PromoOrder::getUserId, userId)
                .orderByDesc(PromoOrder::getCreatedAt));
    }

    @Override
    public List<PromoOrder> getShopOrders(Long shopId) {
        return list(new LambdaQueryWrapper<PromoOrder>()
                .eq(PromoOrder::getShopId, shopId)
                .eq(PromoOrder::getStatus, 1)
                .orderByDesc(PromoOrder::getCreatedAt));
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId) {
        PromoOrder order = getById(orderId);
        if (order == null || order.getStatus() != 0) return false;

        PromoActivity activity = activityService.getById(order.getActivityId());
        if (activity == null || activity.getRemainingTickets() <= 0) {
            throw new RuntimeException("库存不足");
        }
        activity.setRemainingTickets(activity.getRemainingTickets() - 1);
        activityService.updateById(activity);

        order.setStatus(1);
        order.setPaidAt(LocalDateTime.now());
        updateById(order);
        return true;
    }

    @Override
    public Map<String, Object> getOrderWithDetails(Long orderId) {
        PromoOrder order = getById(orderId);
        if (order == null) return null;
        PromoActivity activity = activityService.getById(order.getActivityId());
        User user = userService.getById(order.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("activity", activity);
        result.put("user", user);
        return result;
    }
}