package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("promo_activity")
public class PromoActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private String title;
    private String coverImage;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTickets;
    private Integer remainingTickets;
    private BigDecimal price;
    /** status: 0=draft, 1=published, 2=ended, 3=cancelled */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}