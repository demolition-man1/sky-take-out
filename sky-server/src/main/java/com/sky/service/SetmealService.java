package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealDishVO;

import java.util.List;


public interface SetmealService {
    void update(SetmealDTO setmealDTO);

    Object page(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void deleteBatch(List<Long> ids);

    void saveWithFlavor(SetmealDTO setmealDTO);

//    List<Setmeal> getSetmealByCategoryId(Integer categoryId);
/**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    List<DishItemVO> getDishBySetmealId(Integer setmealId);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
}
