package com.sky.service;

import com.sky.vo.TableStatusVO;

public interface TableService {

    /**
     * 开桌
     * @param tableId
     * @param seatNumber
     */
    void openTable(Long tableId, Integer seatNumber);

    /**
     * 获取桌台状态
     * @param shopId
     * @param storeId
     * @param tableId
     * @return
     */
    TableStatusVO getTableStatus(Long shopId, Long storeId, Long tableId);
}
