package com.taptales.controller;

import com.taptales.entity.User;
import com.taptales.service.UserService;
import com.taptales.util.JwtUtil;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/wechatLogin")
    public Result wechatLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        String nickname = params.get("nickname");
        String avatarUrl = params.get("avatarUrl");
        // TODO: replace code with real openid via wx.code2Session
        String openid = code;
        if (openid == null || openid.isEmpty()) {
            return Result.fail("wechat login failed");
        }
        User user = userService.loginOrRegister(openid, nickname, avatarUrl);
        String token = jwtUtil.generateToken(user.getId(), "user");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("openid", user.getOpenid());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("totalDrunk", user.getTotalDrunk());
        return Result.ok(data);
    }

    @GetMapping("/info")
    public Result getUserInfo() {
        Long userId = com.taptales.util.UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) return Result.fail(401, "not logged in");
        return Result.ok(user);
    }

    @PutMapping("/update")
    public Result updateUserInfo(@RequestBody User user) {
        Long userId = com.taptales.util.UserContext.getUserId();
        User existing = userService.getById(userId);
        if (existing == null) return Result.fail(401, "not logged in");
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getAvatarUrl() != null) existing.setAvatarUrl(user.getAvatarUrl());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        userService.updateById(existing);
        return Result.ok();
    }
}