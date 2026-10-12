package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.PromoOrder;
import com.taptales.entity.Redemption;
import com.taptales.mapper.RedemptionMapper;
import com.taptales.mapper.UserMapper;
import com.taptales.service.PromoOrderService;
import com.taptales.service.RedemptionService;
import com.taptales.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedemptionServiceImpl extends ServiceImpl<RedemptionMapper, Redemption> implements RedemptionService {
    @Autowired
    private PromoOrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Redemption redeem(Long orderId, Long userId, Long drinkId, Integer count) {
        PromoOrder order = orderService.getById(orderId);
        if (order == null) throw new RuntimeException("order not found");
        if (order.getStatus() != 1) throw new RuntimeException("order not paid");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("not your order");

        List<Redemption> existing = list(new LambdaQueryWrapper<Redemption>()
                .eq(Redemption::getOrderId, orderId));
        int alreadyRedeemed = existing.stream().mapToInt(r -> r.getCount()).sum();
        if (alreadyRedeemed >= order.getTotalCount()) {
            throw new RuntimeException("order fully redeemed");
        }
        int canRedeem = order.getTotalCount() - alreadyRedeemed;
        if (count > canRedeem) {
            throw new RuntimeException("redeem count exceeds remaining");
        }

        Redemption redemption = new Redemption();
        redemption.setOrderId(orderId);
        redemption.setUserId(userId);
        redemption.setShopId(order.getShopId());
        redemption.setDrinkId(drinkId);
        redemption.setCount(count);
        redemption.setRedeemedAt(LocalDateTime.now());
        save(redemption);

        userMapper.increaseDrunkCount(userId, count);
        return redemption;
    }

    @Override
    public List<Redemption> getUserRedemptions(Long userId) {
        return list(new LambdaQueryWrapper<Redemption>()
                .eq(Redemption::getUserId, userId)
                .orderByDesc(Redemption::getRedeemedAt));
    }

    @Override
    public List<Redemption> getShopRedemptions(Long shopId) {
        return list(new LambdaQueryWrapper<Redemption>()
                .eq(Redemption::getShopId, shopId)
                .orderByDesc(Redemption::getRedeemedAt));
    }

    @Override
    public List<Map<String, Object>> getLeaderboard(int limit) {
        List<Redemption> all = list();
        Map<Long, Integer> stats = new HashMap<>();
        for (Redemption r : all) {
            stats.merge(r.getUserId(), r.getCount(), Integer::sum);
        }
        List<Map.Entry<Long, Integer>> sorted = stats.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank", i + 1);
            entry.put("userId", sorted.get(i).getKey());
            entry.put("count", sorted.get(i).getValue());
            result.add(entry);
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserRedemptionStats(Long userId) {
        List<Redemption> redemptions = list(new LambdaQueryWrapper<Redemption>()
                .eq(Redemption::getUserId, userId));
        int total = redemptions.stream().mapToInt(r -> r.getCount()).sum();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", total);
        stats.put("redemptionCount", redemptions.size());
        return stats;
    }
}