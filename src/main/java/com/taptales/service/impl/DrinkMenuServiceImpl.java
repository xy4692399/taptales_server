package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.Drink;
import com.taptales.entity.DrinkMenu;
import com.taptales.mapper.DrinkMapper;
import com.taptales.mapper.DrinkMenuMapper;
import com.taptales.service.DrinkMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrinkMenuServiceImpl extends ServiceImpl<DrinkMenuMapper, DrinkMenu> implements DrinkMenuService {
    @Autowired
    private DrinkMapper drinkMapper;

    @Override
    public List<DrinkMenu> getMenusByShopId(Long shopId) {
        return list(new LambdaQueryWrapper<DrinkMenu>()
                .eq(DrinkMenu::getShopId, shopId)
                .eq(DrinkMenu::getStatus, 1)
                .orderByAsc(DrinkMenu::getSortOrder));
    }

    @Override
    public List<Drink> getDrinksByMenuId(Long menuId) {
        return drinkMapper.selectList(new LambdaQueryWrapper<Drink>()
                .eq(Drink::getMenuId, menuId)
                .eq(Drink::getStatus, 1)
                .orderByAsc(Drink::getSortOrder));
    }
}
