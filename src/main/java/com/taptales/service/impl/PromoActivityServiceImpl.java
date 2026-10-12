package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.PromoActivity;
import com.taptales.entity.PromoActivityDrink;
import com.taptales.mapper.PromoActivityDrinkMapper;
import com.taptales.mapper.PromoActivityMapper;
import com.taptales.service.PromoActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromoActivityServiceImpl extends ServiceImpl<PromoActivityMapper, PromoActivity> implements PromoActivityService {
    @Autowired
    private PromoActivityDrinkMapper activityDrinkMapper;

    @Override
    public List<PromoActivity> getActiveActivitiesByShopId(Long shopId) {
        return list(new LambdaQueryWrapper<PromoActivity>()
                .eq(PromoActivity::getShopId, shopId)
                .eq(PromoActivity::getStatus, 1)
                .ge(PromoActivity::getEndTime, LocalDateTime.now())
                .orderByDesc(PromoActivity::getCreatedAt));
    }

    @Override
    public List<PromoActivity> getPublishedActivities() {
        return list(new LambdaQueryWrapper<PromoActivity>()
                .eq(PromoActivity::getStatus, 1)
                .ge(PromoActivity::getStartTime, LocalDateTime.now())
                .le(PromoActivity::getEndTime, LocalDateTime.now())
                .gt(PromoActivity::getRemainingTickets, 0)
                .orderByDesc(PromoActivity::getCreatedAt));
    }

    @Override
    public Map<String, Object> getActivityWithDrinks(Long activityId) {
        PromoActivity activity = getById(activityId);
        if (activity == null) return null;
        List<PromoActivityDrink> drinks = activityDrinkMapper.selectList(
                new LambdaQueryWrapper<PromoActivityDrink>().eq(PromoActivityDrink::getActivityId, activityId));
        Map<String, Object> result = new HashMap<>();
        result.put("activity", activity);
        result.put("drinks", drinks);
        return result;
    }
}
