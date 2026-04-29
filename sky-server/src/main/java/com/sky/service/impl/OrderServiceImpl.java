package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.converter.OrderConverter;
import com.sky.converter.ShoppingCartConverter;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private OrderConverter orderConverter;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartConverter shoppingCartConverter;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //先处理业务异常（地址不存在，购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表中插入一条数据
        Orders orders = orderConverter.toOrders(ordersSubmitDTO);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        //向订单明细表中插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = shoppingCartConverter.toOrderDetail(cart);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //返回OrderSubmitVO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> ordersList = new ArrayList<>();
        if (page.getResult() != null && page.getResult().size() > 0) {
            //  收集所有订单ID
//            List<Long> orderIds = page.getResult().stream()
//                    .map( order -> order.getId())
//                    .collect(Collectors.toList());
            //  上面是使用Stream流的方式收集订单ID，下面是使用传统的for循环方式收集订单ID
            List<Long> orderIds = new ArrayList<>();
            for (Orders orders : page.getResult()) {
                orderIds.add(orders.getId());
            }
            //  批量查询所有订单明细
            List<OrderDetail> allDetails = orderDetailMapper.getByOrderIds(orderIds);

            //  按订单ID分组
//            Map<Long, List<OrderDetail>> detailMap = allDetails.stream()
//                    .collect(Collectors.groupingBy(OrderDetail -> OrderDetail.getOrderId()));
            //  上面是使用Stream流的方式按订单ID分组，下面是使用传统的for循环方式按订单ID分组
            Map<Long, List<OrderDetail>> detailMap = new HashMap<>();
            for (OrderDetail detail : allDetails) {
                Long orderId = detail.getOrderId();
                if (!detailMap.containsKey(orderId)) {
                    detailMap.put(orderId, new ArrayList<>());
                }
                detailMap.get(orderId).add(detail);
            }
            //  组装VO
            for (Orders orders : page.getResult()) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                // 从Map中获取该订单的明细
                List<OrderDetail> orderDetailList = detailMap.getOrDefault(orders.getId(), new ArrayList<>());
                orderVO.setOrderDetailList(orderDetailList);

                ordersList.add(orderVO);
            }
        }
        Long total = page.getTotal();
        return new PageResult(total, ordersList);
    }
/**
 * 订单详情
 *
 * @param id
 * @return
 */
    @Override
    public OrderVO orderDetail(Long id) {
        // 1. 根据id查询订单
        Orders orders = orderMapper.getById(id);
        // 2. 根据订单id查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        // 3. 组装OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }
/**
 * 用户取消订单
 *
 * @param id
 * @return
 */
    @Override
    public Result cancel(Long id) throws Exception {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            return Result.error(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() >2) {
            return Result.error(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 订单处于待接单状态下取消，需要进行退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        return Result.success();
    }
/**
 * 再来一单
 *
 * @param id
 */
    @Override
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
//        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
//            ShoppingCart shoppingCart = new ShoppingCart();
        // 将OrderDetail对象中的属性复制给ShoppingCart对象，上下的语法不同，功能完全相同
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id"); // 排除id字段
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }
/**
 * 条件搜索订单
 *
 * @param ordersPageQueryDTO
 * @return
 */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 部分订单状态，需要额外返回订单菜品信息
                if (orders.getStatus()>2) {
                    List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                    orderVO.setOrderDetailList(orderDetailList);
                }
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
/**
 * 统计订单数据
 * @return
 */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countOrdersByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countOrdersByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countOrdersByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed( confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }
//复用上面的orderDetail方法，直接调用即可
//    @Override
//    public OrderVO getOrderDetailById(Long id) {
//        Orders orders = orderMapper.getById(id);
//        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
//        OrderVO orderVO = new OrderVO();
//        BeanUtils.copyProperties(orders, orderVO);
//        orderVO.setOrderDetailList(orderDetails);
//        return orderVO;
//    }

    @Override
    public void confirm(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(updateOrder);
    }
/**
 * 订单拒绝
 * @param ordersRejectionDTO
 */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
    }
/**
 * 商家取消订单
 * @param ordersCancelDTO
 */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 已支付的需要退款
        if (orders.getPayStatus().equals(Orders.PAID)) {
            weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    orders.getAmount(),
                    orders.getAmount()
            );
            orders.setPayStatus(Orders.REFUND);
        }
        Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }
/**
 * 订单派送
 * @param id
 */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(updateOrder);
    }
/**
 * 订单完成
 * @param id
 */
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(updateOrder);
    }
}
