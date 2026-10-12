package com.taptales.controller;

import com.taptales.entity.ShopOwner;
import com.taptales.service.ShopOwnerService;
import com.taptales.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/owners")
public class ShopOwnerAdminController {
    @Autowired
    private ShopOwnerService shopOwnerService;

    @GetMapping("/list")
    public Result ownerList() {
        List<ShopOwner> owners = shopOwnerService.list();
        return Result.ok(owners);
    }

    @PostMapping("/create")
    public Result createOwner(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");
        String phone = params.get("phone");
        if (shopOwnerService.getByUsername(username) != null) {
            return Result.fail("username already exists");
        }
        ShopOwner owner = new ShopOwner();
        owner.setUsername(username);
        owner.setPassword(md5(password));
        owner.setRealName(realName);
        owner.setPhone(phone);
        owner.setStatus(1);
        shopOwnerService.save(owner);
        return Result.ok(owner.getId());
    }

    @PutMapping("/update/{id}")
    public Result updateOwner(@PathVariable Long id, @RequestBody Map<String, String> params) {
        ShopOwner owner = shopOwnerService.getById(id);
        if (owner == null) return Result.fail("owner not found");
        if (params.get("realName") != null) owner.setRealName(params.get("realName"));
        if (params.get("phone") != null) owner.setPhone(params.get("phone"));
        if (params.get("status") != null) owner.setStatus(Integer.parseInt(params.get("status")));
        shopOwnerService.updateById(owner);
        return Result.ok();
    }

    @PostMapping("/resetPassword/{id}")
    public Result resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        ShopOwner owner = shopOwnerService.getById(id);
        if (owner == null) return Result.fail("owner not found");
        owner.setPassword(md5(params.get("newPassword")));
        shopOwnerService.updateById(owner);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteOwner(@PathVariable Long id) {
        shopOwnerService.removeById(id);
        return Result.ok();
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