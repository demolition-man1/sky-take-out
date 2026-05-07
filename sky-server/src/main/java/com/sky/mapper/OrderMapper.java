package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
     /**
      * 订单分页查询
      * @param ordersPageQueryDTO
      * @return
      */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    @Update("update orders set status = #{status} where id = #{id}")
    void update(Orders orders);

    Integer countOrdersByStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 统计订单金额
     * @param begin 开始时间
     * @param end 结束时间
     * @param status 订单状态
     * @return 金额合计
     */
    Double sumByMap(@Param("begin") LocalDateTime begin,
                    @Param("end") LocalDateTime end,
                    @Param("status") Integer status);
    /**
     * 统计订单金额，按照日期分组（批量查询）
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param completed 订单状态
     * @return key是日期字符串，value是金额合计
     */
    @org.apache.ibatis.annotations.MapKey("dateKey")
    List<Map<String, Object>> sumByDateRange(LocalDateTime beginTime, LocalDateTime endTime, Integer completed);

    @Update("update orders set status = #{orderStatus}, pay_status = #{orderPaidStatus}, checkout_time = #{checkOutTime}" +
            " where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, String orderNumber);

    List<Map<String, Object>> countByDateRange(LocalDateTime beginTime, LocalDateTime endTime);

    List<Map<String, Object>> countValidByDateRange(LocalDateTime beginTime, LocalDateTime endTime);
}
