package com.taptales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taptales.entity.User;
import com.taptales.mapper.UserMapper;
import com.taptales.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    @Override
    public User loginOrRegister(String openid, String nickname, String avatarUrl) {
        User user = getByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setTotalDrunk(0);
            save(user);
        } else {
            if (nickname != null) user.setNickname(nickname);
            if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
            updateById(user);
        }
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }
}
