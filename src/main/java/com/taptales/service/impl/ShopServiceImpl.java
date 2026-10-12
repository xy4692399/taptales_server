package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.Shop;
import com.taptales.mapper.ShopMapper;
import com.taptales.service.ShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {
    @Override
    public Shop getByOwnerId(Long ownerId) {
        return getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getOwnerId, ownerId));
    }
}
