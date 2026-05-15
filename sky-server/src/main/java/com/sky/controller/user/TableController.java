package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.TableService;
import com.sky.vo.TableStatusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userTableController")
@RequestMapping("/user/table")
@Api(tags = "C端-桌台接口")
@Slf4j
public class TableController {

    @Autowired
    private TableService tableService;

    /**
     * 开桌
     * @param tableId
     * @param seatNumber
     * @return
     */
    @GetMapping("/open/{tableId}/{seatNumber}")
    @ApiOperation("开桌")
    public Result openTable(@PathVariable Long tableId, @PathVariable Integer seatNumber) {
        log.info("开桌，桌台ID：{}，座位数：{}", tableId, seatNumber);
        tableService.openTable(tableId, seatNumber);
        return Result.success();
    }

    /**
     * 获取桌台状态
     * @param shopId
     * @param storeId
     * @param tableId
     * @return
     */
    @GetMapping("/tableStatus/{shopId}/{storeId}/{tableId}")
    @ApiOperation("获取桌台状态")
    public Result<TableStatusVO> getTableStatus(@PathVariable Long shopId, 
                                                  @PathVariable Long storeId, 
                                                  @PathVariable Long tableId) {
        log.info("获取桌台状态，店铺ID：{}，门店ID：{}，桌台ID：{}", shopId, storeId, tableId);
        TableStatusVO tableStatusVO = tableService.getTableStatus(shopId, storeId, tableId);
        return Result.success(tableStatusVO);
    }
}
