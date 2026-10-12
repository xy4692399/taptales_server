package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.PromoActivity;

import java.util.List;
import java.util.Map;

public interface PromoActivityService extends IService<PromoActivity> {
    List<PromoActivity> getActiveActivitiesByShopId(Long shopId);
    List<PromoActivity> getPublishedActivities();
    Map<String, Object> getActivityWithDrinks(Long activityId);
}