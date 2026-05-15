package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
        /**
        * 加入购物车
        * @param shoppingCartDTO
        */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void clean();

    /**
     * 修改购物车数量
     * @param shoppingCartDTO
     */
    void updateShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 减菜
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
