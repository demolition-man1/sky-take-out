package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealDishVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    @AutoFill(value = OperationType.UPDATE)
    void update(SetmealDTO setmealDTO);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void updateStatus(Setmeal setmeal);

    void deleteBatch(List<Long> ids);

    List<Setmeal> getSetmealByIds(List<Long> ids);
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal (name, category_id, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{categoryId}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Setmeal setmeal);
//@Select("select * from setmeal where category_id = #{categoryId} and status = 1")
  //  List<Setmeal> getSetmealByCategoryId(Integer categoryId);

    List<DishItemVO> getDishBySetmealId(Integer setmealId);

    List<Setmeal> list(Setmeal setmeal);
}
