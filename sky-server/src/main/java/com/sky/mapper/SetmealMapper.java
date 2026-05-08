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
    /**
     * 插入套餐数据
     * @param setmealDTO
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(SetmealDTO setmealDTO);
    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    /**
     * 修改套餐的起售停售状态
     * @param setmeal
     */
    void updateStatus(Setmeal setmeal);
    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);
    /**
     * 根据id查询套餐和套餐关联的菜品数据
     * @param ids
     * @return
     */

    List<Setmeal> getSetmealByIds(List<Long> ids);
    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal (name, category_id, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{categoryId}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Setmeal setmeal);
//@Select("select * from setmeal where category_id = #{categoryId} and status = 1")
  //  List<Setmeal> getSetmealByCategoryId(Integer categoryId);
    /**
     * 根据套餐id查询包含的菜品列表
     * @param setmealId
     * @return
     */
    List<DishItemVO> getDishBySetmealId(Integer setmealId);
    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 统计套餐数量
     * @param status 套餐状态
     * @return 套餐数量
     */
    Integer countByStatus(Integer status);
}
