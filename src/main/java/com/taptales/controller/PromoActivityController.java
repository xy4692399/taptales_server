package com.taptales.controller;

import com.taptales.entity.PromoActivity;
import com.taptales.entity.PromoActivityDrink;
import com.taptales.entity.Shop;
import com.taptales.mapper.PromoActivityDrinkMapper;
import com.taptales.service.PromoActivityService;
import com.taptales.service.ShopService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/activity")
public class PromoActivityController {
    @Autowired
    private PromoActivityService activityService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private PromoActivityDrinkMapper activityDrinkMapper;

    @GetMapping("/shop/list")
    public Result getShopActivities() {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop shop = shopService.getByOwnerId(ownerId);
        if (shop == null) return Result.fail(401, "shop not found");
        List<PromoActivity> activities = activityService.getActiveActivitiesByShopId(shop.getId());
        return Result.ok(activities);
    }

    @PostMapping("/save")
    public Result saveActivity(@RequestBody Map<String, Object> params) {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop shop = shopService.getByOwnerId(ownerId);
        if (shop == null) return Result.fail(401, "shop not found");

        Long activityId = params.get("id") != null ? ((Number) params.get("id")).longValue() : null;
        PromoActivity activity;
        if (activityId != null) {
            activity = activityService.getById(activityId);
            if (activity == null) return Result.fail("activity not found");
        } else {
            activity = new PromoActivity();
            activity.setShopId(shop.getId());
            activity.setRemainingTickets(0);
            activity.setStatus(0);
        }
        activity.setTitle((String) params.get("title"));
        activity.setCoverImage((String) params.get("coverImage"));
        activity.setDescription((String) params.get("description"));
        activity.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        activity.setTotalTickets((Integer) params.get("totalTickets"));
        activity.setRemainingTickets((Integer) params.get("totalTickets"));

        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        if (startTime != null) activity.setStartTime(LocalDateTime.parse(startTime));
        if (endTime != null) activity.setEndTime(LocalDateTime.parse(endTime));

        activityService.saveOrUpdate(activity);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> drinkList = (List<Map<String, Object>>) params.get("drinks");
        if (drinkList != null) {
            activityDrinkMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromoActivityDrink>()
                            .eq(PromoActivityDrink::getActivityId, activity.getId()));
            for (Map<String, Object> d : drinkList) {
                PromoActivityDrink pad = new PromoActivityDrink();
                pad.setActivityId(activity.getId());
                pad.setDrinkId(((Number) d.get("drinkId")).longValue());
                pad.setCount(((Number) d.get("count")).intValue());
                activityDrinkMapper.insert(pad);
            }
        }
        return Result.ok(activity.getId());
    }

    @PostMapping("/status")
    public Result updateActivityStatus(@RequestBody Map<String, Long> params) {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop shop = shopService.getByOwnerId(ownerId);
        if (shop == null) return Result.fail(401, "shop not found");

        Long activityId = params.get("id");
        Integer status = ((Number) params.get("status")).intValue();
        PromoActivity activity = activityService.getById(activityId);
        if (activity == null) return Result.fail("activity not found");
        if (!activity.getShopId().equals(shop.getId())) return Result.fail("no permission");

        activity.setStatus(status);
        activityService.updateById(activity);
        return Result.ok();
    }

    @GetMapping("/detail/{id}")
    public Result getActivityDetail(@PathVariable Long id) {
        return Result.ok(activityService.getActivityWithDrinks(id));
    }

    @GetMapping("/public/list")
    public Result getPublicActivities() {
        List<PromoActivity> activities = activityService.getPublishedActivities();
        return Result.ok(activities);
    }

    @GetMapping("/public/shop/{shopId}")
    public Result getPublicActivitiesByShop(@PathVariable Long shopId) {
        List<PromoActivity> activities = activityService.getActiveActivitiesByShopId(shopId);
        return Result.ok(activities);
    }
}
