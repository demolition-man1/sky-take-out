package com.sky.converter;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShoppingCartConverter {
    ShoppingCartConverter INSTANCE = Mappers.getMapper(ShoppingCartConverter.class);

    // DTO → Entity
    ShoppingCart toShoppingCart(ShoppingCartDTO shoppingCartDTO);

    // ShoppingCart → OrderDetail
    OrderDetail toOrderDetail(ShoppingCart shoppingCart);
}
