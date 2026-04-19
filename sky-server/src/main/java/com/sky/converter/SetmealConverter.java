package com.sky.converter;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SetmealConverter {
    SetmealConverter INSTANCE = Mappers.getMapper(SetmealConverter.class);

    // DTO → Entity
    Setmeal toSetmeal(SetmealDTO setmealDTO);

    // Entity → VO
    SetmealVO toSetmealVO(Setmeal setmeal);
}
