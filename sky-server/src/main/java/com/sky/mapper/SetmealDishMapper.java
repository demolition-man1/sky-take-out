package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdByDishId(List<Long> dishIds);
@Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteById(Long id);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBySetmealIdBatch(List<Long> ids);
}
