package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success("修改成功");
    }
    @GetMapping("/page")
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        setmealService.page(setmealPageQueryDTO);
        return Result.success(setmealService.page(setmealPageQueryDTO));
    }
@PostMapping("/status/{status}")
@ApiOperation("起售或停售套餐")
@CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐起售或停售：{}", id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
@DeleteMapping
@ApiOperation("批量删除套餐")
@CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
@PostMapping
@ApiOperation("新增套餐")
@CacheEvict(cacheNames = "setmealCache", key =  "#setmealDTO.categoryId")
public Result save(@RequestBody SetmealDTO setmealDTO){
    log.info("新增套餐：{}", setmealDTO);
    setmealService.saveWithFlavor(setmealDTO);
    return Result.success();
    }
}
