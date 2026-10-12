package com.taptales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drink")
public class Drink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long menuId;
    private String drinkCode;
    private String name;
    private String imageUrl;
    private String size;
    private BigDecimal price;
    /** category: 1=hops(green), 2=fruit(purple), 3=connect(gold) */
    private Integer category;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
