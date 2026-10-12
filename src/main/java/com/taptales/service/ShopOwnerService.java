package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.ShopOwner;

public interface ShopOwnerService extends IService<ShopOwner> {
    ShopOwner getByUsername(String username);
    ShopOwner login(String username, String password);
    ShopOwner getOwnerByUserId(Long userId);
}
