package com.sky.converter;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderConverter {
    OrderConverter INSTANCE = Mappers.getMapper(OrderConverter.class);

    // DTO → Entity
    Orders toOrders(OrdersSubmitDTO ordersSubmitDTO);
}