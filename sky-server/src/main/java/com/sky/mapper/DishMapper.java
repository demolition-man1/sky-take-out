package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
    /**
     * 插入菜品数据
     * @param dish
     */
@AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
/**
     * 菜品查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

@Select("select * from dish where id = #{id}")
    Dish getById(Long id);
/**
     * 根据id删除菜品数据
     * @param ids
     */

    void deleteBatch(List<Long> ids);
@Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteById(Long dishId);

    void update(Dish dish);
    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);
    /**
     * 根据ID列表批量查询菜品
     * @param ids
     * @return
     */
    List<Dish> getByIds(List<Long> ids);

    /**
     * 统计菜品数量
     * @param status 菜品状态
     * @return 菜品数量
     */
    Integer countByStatus(Integer status);
}
