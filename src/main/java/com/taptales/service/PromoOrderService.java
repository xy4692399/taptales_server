package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.PromoOrder;

import java.util.List;
import java.util.Map;

public interface PromoOrderService extends IService<PromoOrder> {
    PromoOrder createOrder(Long userId, Long activityId);
    PromoOrder getOrderByNo(String orderNo);
    List<PromoOrder> getUserOrders(Long userId);

    /** Return all orders regardless of status, for manual sync of pending-paid orders */
    List<PromoOrder> getUserOrdersAll(Long userId);
    List<PromoOrder> getShopOrders(Long shopId);
    boolean payOrder(Long orderId);
    Map<String, Object> getOrderWithDetails(Long orderId);
}
