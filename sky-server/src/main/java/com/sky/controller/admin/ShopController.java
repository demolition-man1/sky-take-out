package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController("adminShopController")
@RequestMapping("admin/shop")
@Api(tags = "商户相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @PutMapping("{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable("status") Integer status) {
        log.info("设置店铺营业状态:{}", status == 1);
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();
    }
    @PutMapping("status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("获取店铺营业状态:{}", status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}
