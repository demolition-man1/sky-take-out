package com.sky.mapper;

import com.sky.entity.Table;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TableMapper {

    /**
     * 根据桌台ID查询桌台信息
     * @param tableId
     * @return
     */
    @Select("select * from `table` where id = #{tableId}")
    Table getById(Long tableId);

    /**
     * 根据店铺ID、门店ID、桌台ID查询桌台状态
     * @param shopId
     * @param storeId
     * @param tableId
     * @return
     */
    @Select("select * from `table` where shop_id = #{shopId} and store_id = #{storeId} and id = #{tableId}")
    Table getByShopStoreTable(Long shopId, Long storeId, Long tableId);

    /**
     * 开桌 - 更新桌台状态
     * @param table
     */
    @Update("update `table` set status = #{status}, order_id = #{orderId}, open_time = #{openTime}, update_time = #{updateTime} where id = #{id}")
    void openTable(Table table);

    /**
     * 关闭桌台 - 更新桌台状态为空闲
     * @param tableId
     */
    @Update("update `table` set status = 0, order_id = null, open_time = null, update_time = now() where id = #{tableId}")
    void closeTable(Long tableId);

    /**
     * 查询所有桌台
     * @return
     */
    @Select("select * from `table`")
    List<Table> list();
}
