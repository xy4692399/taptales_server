package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.AdminUser;
import com.taptales.mapper.AdminUserMapper;
import com.taptales.service.AdminUserService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
    @Override
    public AdminUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
    }

    @Override
    public AdminUser login(String username, String password) {
        AdminUser admin = getByUsername(username);
        if (admin == null) return null;
        String md5 = md5(password);
        if (!md5.equals(admin.getPassword())) return null;
        return admin;
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
