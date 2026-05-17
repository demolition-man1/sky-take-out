package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
    
    /**
     * 根据多个订单ID批量查询订单明细
     * @param orderIds
     * @return
     */
    List<OrderDetail> getByOrderIds(List<Long> orderIds);
    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 统计菜品销量
     * @param dishId 菜品ID
     * @return 销量
     */
    @Select("SELECT COALESCE(SUM(number), 0) FROM order_detail WHERE dish_id = #{dishId}")
    Integer countDishSales(Long dishId);

    /**
     * 统计套餐销量
     * @param setmealId 套餐ID
     * @return 销量
     */
    @Select("SELECT COALESCE(SUM(number), 0) FROM order_detail WHERE setmeal_id = #{setmealId}")
    Integer countSetmealSales(Long setmealId);

}
