package com.sky.aspect;

import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class CacheCleanAspect {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @AfterReturning("execution(* com.sky.controller.admin.DishController.save(..)) || " +
            "execution(* com.sky.controller.admin.DishController.update(..))")
    public void cleanDishCache(JoinPoint joinPoint) {
        Object arg = joinPoint.getArgs()[0];
        try {
            Long categoryId = (Long) arg.getClass().getMethod("getCategoryId").invoke(arg);
            String key = "dish_" + categoryId;
            redisTemplate.delete(key);
            log.info("清理菜品缓存: {}", key);
        } catch (Exception e) {
            log.error("清理菜品缓存失败", e);
        }
    }

    @AfterReturning("execution(* com.sky.controller.admin.DishController.delete(..))")
    public void cleanDishCacheOnDelete(JoinPoint joinPoint) {
        try {
            Object arg = joinPoint.getArgs()[0];
            if (arg instanceof List) {
                List<Long> ids = (List<Long>) arg;
                // 批量查询所有被删除的菜品（一次SQL）
                List<Dish> dishes = dishMapper.getByIds(ids);

                // 提取这些菜品对应的分类 ID (去重)
                Set<Long> categoryIds = dishes.stream()
                        .filter(dish -> dish != null)
                        .map(Dish::getCategoryId)
                        .collect(Collectors.toSet());

                // 清理所有相关分类的缓存
                for (Long categoryId : categoryIds) {
                    String key = "dish_" + categoryId;
                    redisTemplate.delete(key);
                    log.info("批量删除后清理菜品缓存: {}", key);
                }
            }
        } catch (Exception e) {
            log.error("清理菜品缓存失败", e);
        }
    }
}

