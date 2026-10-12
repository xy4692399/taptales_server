package com.taptales.controller;

import com.taptales.entity.Shop;
import com.taptales.entity.ShopOwner;
import com.taptales.service.ShopOwnerService;
import com.taptales.service.ShopService;
import com.taptales.util.JwtUtil;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopOwnerService shopOwnerService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/owner/login")
    public Result ownerLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        ShopOwner owner = shopOwnerService.login(username, password);
        if (owner == null) return Result.fail(401, "invalid credentials");
        String token = jwtUtil.generateToken(owner.getId(), "shopOwner");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("ownerId", owner.getId());
        data.put("username", owner.getUsername());
        data.put("realName", owner.getRealName());
        Shop shop = shopService.getByOwnerId(owner.getId());
        data.put("shop", shop);
        return Result.ok(data);
    }

    @GetMapping("/owner/info")
    public Result ownerInfo() {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        ShopOwner owner = shopOwnerService.getById(ownerId);
        if (owner == null) return Result.fail(401, "not logged in");
        Shop shop = shopService.getByOwnerId(ownerId);
        Map<String, Object> data = new HashMap<>();
        data.put("owner", owner);
        data.put("shop", shop);
        return Result.ok(data);
    }

    @GetMapping("/list")
    public Result shopList() {
        List<Shop> shops = shopService.list();
        return Result.ok(shops);
    }

    @PutMapping("/update")
    public Result updateShop(@RequestBody Shop shop) {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop existing = shopService.getByOwnerId(ownerId);
        if (existing == null) return Result.fail(401, "shop not found");
        if (shop.getName() != null) existing.setName(shop.getName());
        if (shop.getAddress() != null) existing.setAddress(shop.getAddress());
        if (shop.getPhone() != null) existing.setPhone(shop.getPhone());
        if (shop.getDescription() != null) existing.setDescription(shop.getDescription());
        if (shop.getLogoUrl() != null) existing.setLogoUrl(shop.getLogoUrl());
        shopService.updateById(existing);
        return Result.ok();
    }

    @PutMapping("/status")
    public Result updateShopStatus(@RequestBody Map<String, Integer> params) {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop shop = shopService.getByOwnerId(ownerId);
        if (shop == null) return Result.fail(401, "shop not found");
        shop.setStatus(params.get("status"));
        shopService.updateById(shop);
        return Result.ok();
    }
}