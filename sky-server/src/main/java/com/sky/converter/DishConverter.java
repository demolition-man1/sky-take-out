package com.sky.converter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
@Mapper(componentModel = "spring")
public interface DishConverter {
    DishConverter INSTANCE = Mappers.getMapper(DishConverter.class);
    DishDTO toDishDTO(Dish dish);
}
