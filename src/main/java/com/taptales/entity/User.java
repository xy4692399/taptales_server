package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usr")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionId;
    private String nickname;
    private String avatarUrl;
    private String phone;
    @TableField("total_drunk")
    private Integer totalDrunk;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}