package com.taptales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taptales.entity.User;

public interface UserService extends IService<User> {
    User getByOpenid(String openid);
    User loginOrRegister(String openid, String nickname, String avatarUrl);
    User getUserById(Long userId);
}
