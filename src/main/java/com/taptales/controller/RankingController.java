package com.taptales.controller;

import com.taptales.entity.Redemption;
import com.taptales.entity.User;
import com.taptales.service.RedemptionService;
import com.taptales.service.UserService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    @Autowired
    private RedemptionService redemptionService;
    @Autowired
    private UserService userService;

    @GetMapping("/leaderboard")
    public Result leaderboard(@RequestParam(defaultValue = "50") int limit) {
        List<Map<String, Object>> raw = redemptionService.getLeaderboard(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : raw) {
            Long userId = ((Number) item.get("userId")).longValue();
            User user = userService.getById(userId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", item.get("rank"));
            row.put("userId", userId);
            row.put("nickname", user != null ? user.getNickname() : "unknown");
            row.put("avatarUrl", user != null ? user.getAvatarUrl() : null);
            row.put("count", item.get("count"));
            result.add(row);
        }
        return Result.ok(result);
    }

    @GetMapping("/my")
    public Result myStats() {
        Long userId = com.taptales.util.UserContext.getUserId();
        User user = userService.getById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("totalDrunk", user != null ? user.getTotalDrunk() : 0);
        data.put("stats", redemptionService.getUserRedemptionStats(userId));
        return Result.ok(data);
    }

    @GetMapping("/my/redemptions")
    public Result myRedemptions() {
        Long userId = com.taptales.util.UserContext.getUserId();
        return Result.ok(redemptionService.getUserRedemptions(userId));
    }
}