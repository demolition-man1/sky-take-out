package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/category")
@Api(tags = "C端-分类接口")
@Slf4j
public class CategoryController {
@Autowired
private CategoryService categoryService;
/**
 * 根据类型查询分类
 */
@GetMapping("/list")
public Result<List<Category>> list(Integer type) {  // 参数改为 type，可选
    log.info("根据类型查询分类: {}", type);
    List<Category> categories = categoryService.list(type);  // 调用 list 方法
    return Result.success(categories);  // 返回列表
     }
}
