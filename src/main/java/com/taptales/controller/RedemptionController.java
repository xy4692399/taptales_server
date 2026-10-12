package com.taptales.controller;

import com.taptales.entity.Redemption;
import com.taptales.service.RedemptionService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/redemption")
public class RedemptionController {
    @Autowired
    private RedemptionService redemptionService;

    @GetMapping("/shop/{shopId}")
    public Result getShopRedemptions(@PathVariable Long shopId) {
        return Result.ok(redemptionService.getShopRedemptions(shopId));
    }
}