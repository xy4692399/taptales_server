package com.taptales.controller;

import com.taptales.entity.Shop;
import com.taptales.service.ShopService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shops")
public class ShopAdminController {
    @Autowired
    private ShopService shopService;

    @GetMapping("/list")
    public Result shopList() {
        List<Shop> shops = shopService.list();
        return Result.ok(shops);
    }

    @PostMapping("/create")
    public Result createShop(@RequestBody Map<String, Object> params) {
        Long ownerId = ((Number) params.get("ownerId")).longValue();
        Shop shop = new Shop();
        shop.setOwnerId(ownerId);
        shop.setName((String) params.get("name"));
        shop.setAddress((String) params.get("address"));
        shop.setPhone((String) params.get("phone"));
        shop.setDescription((String) params.get("description"));
        shop.setStatus(1);
        shopService.save(shop);
        return Result.ok(shop.getId());
    }

    @PutMapping("/update/{id}")
    public Result updateShop(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Shop shop = shopService.getById(id);
        if (shop == null) return Result.fail("shop not found");
        if (params.get("name") != null) shop.setName((String) params.get("name"));
        if (params.get("address") != null) shop.setAddress((String) params.get("address"));
        if (params.get("phone") != null) shop.setPhone((String) params.get("phone"));
        if (params.get("status") != null) shop.setStatus((Integer) params.get("status"));
        shopService.updateById(shop);
        return Result.ok();
    }
}