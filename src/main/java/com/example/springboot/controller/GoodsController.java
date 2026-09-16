package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICollectService;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private IGoodsService goodsService;
    @Resource
    private ICollectService collectService;
    @Resource
    private IUserService userService;

    private void wrapGoods(Goods goods) {
        if (goods.getUserId() != null) {
            User user = userService.getById(goods.getUserId());
            if (user != null) {
                goods.setIsAuth(user.getIsAuth());
                goods.setSchool(user.getSchool());
            }
        }
    }

    private List<Goods> wrapGoodsList(List<Goods> list) {
        if (CollectionUtil.isEmpty(list)) {
            return list;
        }
        for (Goods goods : list) {
            wrapGoods(goods);
        }
        return list;
    }

    @PostMapping
    public Result save(@RequestBody Goods goods) {
        if (goods.getId() == null) {
            goods.setUserId(TokenUtils.getCurrentUser().getId());
            goods.setStatus("已上架");
            goods.setNum(0);
        }
        return Result.success(goodsService.saveOrUpdate(goods));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(goodsService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(goodsService.removeByIds(ids));
    }

    @GetMapping("/front")
    public Result findAllFront() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Goods::getNum);
        wrapper.eq(Goods::getStatus,"已上架");
        wrapper.last("limit 12");
        List<Goods> goodsList = goodsService.list(wrapper);
        return Result.success(wrapGoodsList(goodsList));
    }

    @GetMapping("/user/{id}")
    public Result user(@PathVariable Integer id) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getUserId, id);
        return Result.success(wrapGoodsList(goodsService.list(wrapper)));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(wrapGoodsList(goodsService.list()));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getItemId, id);
        wrapper.eq(Collect::getUserId, TokenUtils.getCurrentUser().getId());
        Collect one = collectService.getOne(wrapper);
        goods.setIsCollected(one != null);

        wrapGoods(goods);

        return Result.success(goods);
    }

    @GetMapping("/front/page")
    public Result findFrontPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam Integer typeId,
                           @RequestParam String sortBy,
                           @RequestParam(defaultValue = "") String country,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getStatus,"已上架");

        if (StrUtil.equals(sortBy,"all")) {
            queryWrapper.orderByDesc(Goods::getNum);
        } else if (StrUtil.equals(sortBy,"new")) {
            queryWrapper.orderByDesc(Goods::getDate);
        } else if (StrUtil.equals(sortBy,"price")) {
            queryWrapper.orderByAsc(Goods::getPrice);
        }

        if (typeId != 0) {
            queryWrapper.eq(Goods::getTypeId, typeId);
        }

        if (StrUtil.isNotBlank(country)) {
            queryWrapper.eq(Goods::getCountry, country);
        }

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        Page<Goods> page = goodsService.page(new Page<>(pageNum, pageSize), queryWrapper);
        wrapGoodsList(page.getRecords());
        return Result.success(page);
    }

    @GetMapping("/collect/page")
    public Result findCollectPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,TokenUtils.getCurrentUser().getId());
        List<Collect> collectList = collectService.list(wrapper);

        //用户没收藏
        if(CollectionUtil.isEmpty(collectList)){
            return Result.success(new Page<>(pageNum, pageSize));
        }

        List<Integer> ids = new ArrayList<>();

        //拿到收藏ID放到ids里
        for (Collect collect : collectList) {
            ids.add(collect.getItemId());
        }

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Goods::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        queryWrapper.in(Goods::getId,ids);

        Page<Goods> page = goodsService.page(new Page<>(pageNum, pageSize), queryWrapper);
        wrapGoodsList(page.getRecords());
        return Result.success(page);
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String country,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Goods::getId);

        if (StrUtil.isNotBlank(country)) {
            queryWrapper.eq(Goods::getCountry, country);
        }

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        Page<Goods> page = goodsService.page(new Page<>(pageNum, pageSize), queryWrapper);
        wrapGoodsList(page.getRecords());
        return Result.success(page);
    }

}

