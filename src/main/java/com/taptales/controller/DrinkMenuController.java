package com.taptales.controller;

import com.taptales.entity.Drink;
import com.taptales.entity.DrinkMenu;
import com.taptales.entity.Shop;
import com.taptales.mapper.DrinkMapper;
import com.taptales.service.DrinkMenuService;
import com.taptales.service.ShopService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class DrinkMenuController {
    @Autowired
    private DrinkMenuService menuService;
    @Autowired
    private DrinkMapper drinkMapper;
    @Autowired
    private ShopService shopService;
    @Value("${file.upload-path}")
    private String uploadPath;

    @GetMapping("/all")
    public Result getAllMenus() {
        List<DrinkMenu> menus = menuService.list();
        return Result.ok(menus);
    }

    @GetMapping("/shop/{shopId}")
    public Result getMenusByShop(@PathVariable Long shopId) {
        List<DrinkMenu> menus = menuService.getMenusByShopId(shopId);
        return Result.ok(menus);
    }

    @GetMapping("/drinks/{menuId}")
    public Result getDrinksByMenu(@PathVariable Long menuId) {
        List<Drink> drinks = menuService.getDrinksByMenuId(menuId);
        return Result.ok(drinks);
    }

    @PostMapping("/create")
    public Result createMenu(@RequestBody DrinkMenu menu) {
        Long ownerId = com.taptales.util.UserContext.getUserId();
        Shop shop = shopService.getByOwnerId(ownerId);
        if (shop == null) return Result.fail(401, "shop not found");
        menu.setShopId(shop.getId());
        menu.setSortOrder(0);
        menu.setStatus(1);
        menuService.save(menu);
        return Result.ok(menu.getId());
    }

    @PutMapping("/update/{id}")
    public Result updateMenu(@PathVariable Long id, @RequestBody DrinkMenu menu) {
        DrinkMenu existing = menuService.getById(id);
        if (existing == null) return Result.fail("menu not found");
        if (menu.getName() != null) existing.setName(menu.getName());
        if (menu.getStatus() != null) existing.setStatus(menu.getStatus());
        if (menu.getSortOrder() != null) existing.setSortOrder(menu.getSortOrder());
        menuService.updateById(existing);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteMenu(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/drink/create")
    public Result createDrink(@RequestBody Drink drink) {
        drink.setSortOrder(0);
        drink.setStatus(1);
        if (drink.getCategory() == null) drink.setCategory(1);
        drinkMapper.insert(drink);
        return Result.ok(drink.getId());
    }

    @PutMapping("/drink/update/{id}")
    public Result updateDrink(@PathVariable Long id, @RequestBody Drink drink) {
        Drink existing = drinkMapper.selectById(id);
        if (existing == null) return Result.fail("drink not found");
        if (drink.getName() != null) existing.setName(drink.getName());
        if (drink.getImageUrl() != null) existing.setImageUrl(drink.getImageUrl());
        if (drink.getSize() != null) existing.setSize(drink.getSize());
        if (drink.getPrice() != null) existing.setPrice(drink.getPrice());
        if (drink.getCategory() != null) existing.setCategory(drink.getCategory());
        if (drink.getDescription() != null) existing.setDescription(drink.getDescription());
        if (drink.getStatus() != null) existing.setStatus(drink.getStatus());
        if (drink.getSortOrder() != null) existing.setSortOrder(drink.getSortOrder());
        drinkMapper.updateById(existing);
        return Result.ok();
    }

    @DeleteMapping("/drink/delete/{id}")
    public Result deleteDrink(@PathVariable Long id) {
        Drink drink = drinkMapper.selectById(id);
        // 删除数据库记录前先删图片文件
        if (drink != null && drink.getImageUrl() != null) {
            deleteImageFile(drink.getImageUrl());
        }
        drinkMapper.deleteById(id);
        return Result.ok();
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        // 提取文件名：http://localhost:8083/uploads/xxx.jpg -> xxx.jpg
        String filename = imageUrl;
        int idx = imageUrl.lastIndexOf('/');
        if (idx >= 0) filename = imageUrl.substring(idx + 1);
        File file = new File(uploadPath, filename);
        if (file.exists()) file.delete();
    }
}
