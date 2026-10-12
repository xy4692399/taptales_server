package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.AdminUser;

public interface AdminUserService extends IService<AdminUser> {
    AdminUser getByUsername(String username);
    AdminUser login(String username, String password);
}
