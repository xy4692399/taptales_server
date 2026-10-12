package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.Drink;
import com.taptales.entity.DrinkMenu;

import java.util.List;

public interface DrinkMenuService extends IService<DrinkMenu> {
    List<DrinkMenu> getMenusByShopId(Long shopId);
    List<Drink> getDrinksByMenuId(Long menuId);
}
