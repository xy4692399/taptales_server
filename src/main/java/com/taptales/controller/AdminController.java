package com.taptales.controller;

import com.taptales.entity.AdminUser;
import com.taptales.service.AdminUserService;
import com.taptales.util.JwtUtil;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        AdminUser admin = adminUserService.login(username, password);
        if (admin == null) return Result.fail(401, "invalid credentials");
        String token = jwtUtil.generateToken(admin.getId(), "admin");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("realName", admin.getRealName());
        return Result.ok(data);
    }

    @GetMapping("/info")
    public Result info() {
        Long adminId = com.taptales.util.UserContext.getUserId();
        AdminUser admin = adminUserService.getById(adminId);
        if (admin == null) return Result.fail(401, "not logged in");
        return Result.ok(admin);
    }
}