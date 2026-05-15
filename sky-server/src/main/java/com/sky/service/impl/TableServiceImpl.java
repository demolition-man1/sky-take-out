package com.sky.service.impl;

import com.sky.entity.Table;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.TableMapper;
import com.sky.service.TableService;
import com.sky.vo.TableStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TableServiceImpl implements TableService {

    @Autowired
    private TableMapper tableMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 开桌
     * @param tableId
     * @param seatNumber
     */
    @Override
    public void openTable(Long tableId, Integer seatNumber) {
        log.info("开桌，桌台ID：{}，座位数：{}", tableId, seatNumber);
        
        // 查询桌台信息
        Table table = tableMapper.getById(tableId);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        
        // 检查桌台状态
        if (table.getStatus() == 1) {
            throw new RuntimeException("桌台正在使用中");
        }
        
        // 更新桌台状态为使用中
        table.setStatus(1);
        table.setOpenTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        tableMapper.openTable(table);
    }

    /**
     * 获取桌台状态
     * @param shopId
     * @param storeId
     * @param tableId
     * @return
     */
    @Override
    public TableStatusVO getTableStatus(Long shopId, Long storeId, Long tableId) {
        log.info("获取桌台状态，店铺ID：{}，门店ID：{}，桌台ID：{}", shopId, storeId, tableId);
        
        // 查询桌台信息
        Table table = tableMapper.getByShopStoreTable(shopId, storeId, tableId);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        
        // 转换为VO
        TableStatusVO tableStatusVO = new TableStatusVO();
        BeanUtils.copyProperties(table, tableStatusVO);
        tableStatusVO.setTableId(table.getId());
        
        // 如果有订单，查询订单金额
        if (table.getOrderId() != null) {
            // TODO: 查询订单金额
            tableStatusVO.setOrderAmount(0.0);
        }
        
        return tableStatusVO;
    }
}
