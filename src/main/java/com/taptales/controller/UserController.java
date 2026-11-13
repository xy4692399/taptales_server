package com.taptales.controller;

import com.taptales.config.WxPayConfig;
import com.taptales.entity.User;
import com.taptales.service.UserService;
import com.taptales.util.JwtUtil;
import com.taptales.util.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WxPayConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/wechatLogin")
    public Result wechatLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        String nickname = params.get("nickname");
        String avatarUrl = params.get("avatarUrl");
        if (code == null || code.isEmpty()) {
            return Result.fail("缺少登录凭证");
        }
        String openid;
        try {
            String sessionUrl = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                config.getAppId(), config.getAppSecret(), code);
            ResponseEntity<String> resp = restTemplate.getForEntity(sessionUrl, String.class);
            log.info("wx.code2Session response: status={}, body={}", resp.getStatusCode(), resp.getBody());
            JsonNode json = objectMapper.readTree(resp.getBody());
            if (json.has("errcode")) {
                int errcode = json.get("errcode").asInt();
                String errmsg = json.has("errmsg") ? json.get("errmsg").asText() : "unknown";
                log.warn("wx.code2Session failed: errcode={}, errmsg={}", errcode, errmsg);
                return Result.fail("微信登录失败(errcode=" + errcode + "): " + errmsg);
            }
            openid = json.has("openid") ? json.get("openid").asText() : null;
            if (openid == null || openid.isEmpty()) {
                return Result.fail("微信登录失败：未获取到 openid");
            }
        } catch (Exception e) {
            log.error("wx.code2Session error: {}", e.getMessage(), e);
            return Result.fail("微信登录失败，请重试");
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
