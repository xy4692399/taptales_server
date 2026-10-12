package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.ShopOwner;
import com.taptales.mapper.ShopOwnerMapper;
import com.taptales.service.ShopOwnerService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class ShopOwnerServiceImpl extends ServiceImpl<ShopOwnerMapper, ShopOwner> implements ShopOwnerService {
    @Override
    public ShopOwner getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<ShopOwner>().eq(ShopOwner::getUsername, username));
    }

    @Override
    public ShopOwner login(String username, String password) {
        ShopOwner owner = getByUsername(username);
        if (owner == null) return null;
        String md5 = md5(password);
        if (!md5.equals(owner.getPassword())) return null;
        if (owner.getStatus() != null && owner.getStatus() == 0) return null;
        return owner;
    }

    @Override
    public ShopOwner getOwnerByUserId(Long userId) {
        return getById(userId);
    }

    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return str;
        }
    }
}
