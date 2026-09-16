package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Banner;
import com.example.springboot.entity.Notice;
import com.example.springboot.service.IBannerService;
import com.example.springboot.service.INoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Resource
    private IBannerService bannerService;

    @PostMapping
    public Result save(@RequestBody Banner banner) {
        return Result.success(bannerService.saveOrUpdate(banner));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(bannerService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(bannerService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(bannerService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(bannerService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Banner::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Banner::getName, keyword);
        }

        return Result.success(bannerService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

