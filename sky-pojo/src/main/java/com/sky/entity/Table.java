package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //桌台编号
    private String tableNumber;

    //店铺ID
    private Long shopId;

    //门店ID
    private Long storeId;

    //座位数
    private Integer seatCount;

    //桌台状态 0-空闲 1-使用中 2-已预订
    private Integer status;

    //当前订单ID
    private Long orderId;

    //开桌时间
    private LocalDateTime openTime;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}
