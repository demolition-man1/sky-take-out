package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
@GetMapping("/historyOrders")
@ApiOperation("查看历史订单")
public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("历史订单查询：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.page(ordersPageQueryDTO);
            return Result.success(pageResult);
    }
    /**
     * 订单详情
     *
     * @param id
     * @return
     */
@GetMapping("/orderDetail/{id}")
@ApiOperation("订单详情")
public Result<OrderVO> orderDetail(@PathVariable Long id)  {
       OrderVO orderVO = orderService.orderDetail(id);
       log.info("订单详情查询：{}", id);
       return Result.success(orderVO);
    }
    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
public Result cancel(@PathVariable Long id) throws Exception{
    log.info("取消订单：{}", id);
    return orderService.cancel(id);
    }
    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
public Result repetition(@PathVariable Long id) {
    log.info("再来一单：{}", id);
    orderService.repetition(id);
    return Result.success();
    }
}
