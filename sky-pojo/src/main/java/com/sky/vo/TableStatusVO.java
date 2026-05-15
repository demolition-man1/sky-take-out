package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台状态VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //桌台ID
    private Long tableId;

    //桌台编号
    private String tableNumber;

    //桌台状态 0-空闲 1-使用中 2-已预订
    private Integer status;

    //座位数
    private Integer seatCount;

    //当前订单ID
    private Long orderId;

    //开桌时间
    private LocalDateTime openTime;

    //订单金额
    private Double orderAmount;
}
