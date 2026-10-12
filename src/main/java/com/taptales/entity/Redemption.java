package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("redemption")
public class Redemption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long shopId;
    private Long drinkId;
    private Integer count;
    private LocalDateTime redeemedAt;
}
