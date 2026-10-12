package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("promo_activity_drink")
public class PromoActivityDrink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long drinkId;
    private Integer count;
}
