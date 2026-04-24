package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.converter.SetmealConverter;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealDishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealConverter setmealConverter;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
/**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Long setmealId = setmealDTO.getId();
        //更新套餐主表
        setmealMapper.update(setmealDTO);
        //删除旧的套餐-菜品关联
        setmealDishMapper.deleteById(setmealId);
        //插入新的套餐-菜品关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
/**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
/**
     * 套餐起售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
           Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
           setmealMapper.updateStatus(setmeal);
    }
/**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前所选的套餐是否正在售卖中(在循环中多次调用数据库，不好)
//        for (Long id : ids) {
//            Setmeal setmeal = setmealMapper.getById(id);
//            if (setmeal.getStatus() == StatusConstant.ENABLE) {
//                throw new RuntimeException("套餐正在售卖中，不能删除");
//            }
//        }
        List<Setmeal> setmeals = setmealMapper.getSetmealByIds(ids);
//        for (int i = 0; i < setmeals.size(); i++) {
//            Setmeal setmeal = setmeals.get(i);
//            if (setmeal.getStatus() == StatusConstant.ENABLE) {
//                throw new RuntimeException("套餐正在售卖中，不能删除");
//            }
//        }
        //判断当前所选的套餐是否正在售卖中(优化)
        for (Setmeal setmeal : setmeals) {
            if(setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new RuntimeException("套餐正在售卖中，不能删除");
            }
        }
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBySetmealIdBatch(ids);
    }
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void saveWithFlavor(SetmealDTO setmealDTO) {
        //传统版本
//        // 创建套餐实体对象
//        Setmeal setmeal = new Setmeal();
//        BeanUtils.copyProperties(setmealDTO, setmeal);
//        // 向套餐表插入1条数据
//        setmealMapper.insert(setmeal);
//        // 获取数据库生成的套餐ID
//        Long setmealId = setmeal.getId();
//        // 处理套餐-菜品关联关系
//        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
//        if(setmealDishes != null && !setmealDishes.isEmpty()){
//            // 为每个关联菜品设置套餐ID
//            setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
//            // 批量插入套餐-菜品关联数据
//            setmealDishMapper.insertBatch(setmealDishes);
//        }
        // 使用 MapStruct 转换 DTO → Entity（弃用了BeanUtils）
        Setmeal setmeal = setmealConverter.toSetmeal(setmealDTO);
        // 向套餐表插入1条数据
        setmealMapper.insert(setmeal);
        // 获取数据库生成的套餐ID
        Long setmealId = setmeal.getId();
        // 处理套餐-菜品关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            // 为每个关联菜品设置套餐ID
            setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
            // 批量插入套餐-菜品关联数据
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }
///**
//     * 根据分类ID查询套餐
//     * @param categoryId
//     * @return
//     */
//    @Override
//    public List<Setmeal> getSetmealByCategoryId(Integer categoryId) {
//        List<Setmeal> list = setmealMapper.getSetmealByCategoryId(categoryId);
//        return list;
//    }
/**
     * 根据套餐ID查询套餐中菜品
     * @param setmealId
     * @return
     */
    @Override
    public List<DishItemVO> getDishBySetmealId(Integer setmealId) {
        return setmealMapper.getDishBySetmealId(setmealId);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

}
