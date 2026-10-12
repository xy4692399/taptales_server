package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.Shop;

public interface ShopService extends IService<Shop> {
    Shop getByOwnerId(Long ownerId);
}
