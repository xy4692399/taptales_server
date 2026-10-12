package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("promo_order")
public class PromoOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long activityId;
    private Long shopId;
    private BigDecimal totalPrice;
    /** total cup count */
    private Integer totalCount;
    /** status: 0=pending, 1=paid, 2=refunded, 3=cancelled */
    private Integer status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}