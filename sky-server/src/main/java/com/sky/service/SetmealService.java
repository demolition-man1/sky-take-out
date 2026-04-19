package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealService {
    void update(SetmealDTO setmealDTO);

    Object page(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void deleteBatch(List<Long> ids);

    void saveWithFlavor(SetmealDTO setmealDTO);
}
