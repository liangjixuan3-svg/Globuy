package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Banner;
import com.example.springboot.entity.Collect;
import com.example.springboot.service.IBannerService;
import com.example.springboot.service.ICollectService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collectService;

    @PostMapping
    public Result save(@RequestBody Collect collect) {
        Integer userId = TokenUtils.getCurrentUser().getId();
        collect.setUserId(userId);

        //表中建立索引，避免重复收藏，所以正常运行到这会报错。因此捕获异常实现再次点击取消收藏
        try{
            collectService.saveOrUpdate(collect);
        }catch (Exception e){
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getUserId, userId);
            queryWrapper.eq(Collect::getItemId, collect.getItemId());
            collectService.remove(queryWrapper);

            return Result.error("605","取消收藏成功");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, TokenUtils.getCurrentUser().getId());
        queryWrapper.eq(Collect::getItemId, id);
        collectService.remove(queryWrapper);

        return Result.success( );
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(collectService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(collectService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(collectService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Collect::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Collect::getUserId, keyword);
        }

        return Result.success(collectService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

