package com.sky.mapper;

import com.sky.entity.Orders;
import org.mapstruct.Mapper;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
}
