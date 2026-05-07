package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealDishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<Setmeal>> getByCategoryId(@RequestParam(value = "categoryId", required = true) Integer categoryId) {
//        log.info("查询套餐信息");
//        List<Setmeal> list = setmealService.getSetmealByCategoryId(categoryId);
//        return Result.success(list);
//    }
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }
    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> getDishBySetmealId(@PathVariable Integer id) {
        log.info("查询套餐包含的菜品，套餐id：{}", id);
        List<DishItemVO> list = setmealService.getDishBySetmealId(id);
        return Result.success(list);
    }
}
