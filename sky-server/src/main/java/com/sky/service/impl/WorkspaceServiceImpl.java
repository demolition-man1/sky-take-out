package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        List<Map<String, Object>> orderCountList = orderMapper.countByDateRange(begin, end);
        Integer totalOrderCount = 0;
        for (Map<String, Object> map : orderCountList) {
            totalOrderCount += ((Number) map.get("count")).intValue();
        }

        Double turnover = orderMapper.sumByMap(begin, end, Orders.COMPLETED);
        turnover = turnover == null ? 0.0 : turnover;

        List<Map<String, Object>> validOrderCountList = orderMapper.countValidByDateRange(begin, end);
        Integer validOrderCount = 0;
        for (Map<String, Object> map : validOrderCountList) {
            validOrderCount += ((Number) map.get("count")).intValue();
        }

        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        List<Map<String, Object>> newUserList = userMapper.sumNewUser(begin, end);
        Integer newUsers = 0;
        for (Map<String, Object> map : newUserList) {
            newUsers += ((Number) map.get("total")).intValue();
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        Integer waitingOrders = orderMapper.countByMap(begin, end, Orders.TO_BE_CONFIRMED);

        Integer deliveredOrders = orderMapper.countByMap(begin, end, Orders.CONFIRMED);

        Integer completedOrders = orderMapper.countByMap(begin, end, Orders.COMPLETED);

        Integer cancelledOrders = orderMapper.countByMap(begin, end, Orders.CANCELLED);

        Integer allOrders = orderMapper.countByMap(begin, end, null);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countByStatus(StatusConstant.ENABLE);

        Integer discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countByStatus(StatusConstant.ENABLE);

        Integer discontinued = setmealMapper.countByStatus(StatusConstant.DISABLE);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
