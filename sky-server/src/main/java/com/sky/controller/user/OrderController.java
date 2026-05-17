package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.ShoppingCart;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderPaymentVO;
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
    @Autowired
    private ShoppingCartService shoppingCartService;
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
public Result<OrderVO> orderDetail(@PathVariable("id") Long id)  {
       log.info("订单详情查询，ID：{}", id);
       if (id == null) {
           log.error("订单ID为空，请检查前端传参");
           return Result.error("订单ID不能为空");
       }
       OrderVO orderVO = orderService.orderDetail(id);
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

    @GetMapping("/reminder/{id}")
    @ApiOperation("订单提醒")
    public Result reminder(@PathVariable("id") Long id) {
        orderService.reminder(id);
        return Result.success();
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 加菜 - 其实就是添加到购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/addDish")
    @ApiOperation("加菜")
    public Result addDish(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("加菜，商品信息为：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 减菜 - 从购物车中减少数量
     * @param tableId
     * @param dishId
     * @return
     */
    @GetMapping("/decreaseDish/{tableId}/{dishId}")
    @ApiOperation("减菜")
    public Result decreaseDish(@PathVariable Long tableId, @PathVariable Long dishId) {
        log.info("减菜，桌台ID：{}，菜品ID：{}", tableId, dishId);
        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        shoppingCartDTO.setDishId(dishId);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 清空购物车
     * @param tableId
     * @return
     */
    @GetMapping("/cleanShopCart/{tableId}")
    @ApiOperation("清空购物车")
    public Result cleanShopCart(@PathVariable Long tableId) {
        log.info("清空购物车，桌台ID：{}", tableId);
        shoppingCartService.clean();
        return Result.success();
    }

    /**
     * 获取购物车菜品
     * @param tableId
     * @return
     */
    @GetMapping("/shopCart/{tableId}")
    @ApiOperation("获取购物车菜品")
    public Result<List<ShoppingCart>> getShopCart(@PathVariable Long tableId) {
        log.info("获取购物车菜品，桌台ID：{}", tableId);
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

}
