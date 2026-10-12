package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.Redemption;

import java.util.List;
import java.util.Map;

public interface RedemptionService extends IService<Redemption> {
    Redemption redeem(Long orderId, Long userId, Long drinkId, Integer count);
    List<Redemption> getUserRedemptions(Long userId);
    List<Redemption> getShopRedemptions(Long shopId);
    List<Map<String, Object>> getLeaderboard(int limit);
    Map<String, Object> getUserRedemptionStats(Long userId);
}
