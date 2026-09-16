package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Address;
import com.example.springboot.service.IAddressService;
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
@RequestMapping("/address")
public class AddressController {

    @Resource
    private IAddressService addressService;

    @PostMapping
    public Result save(@RequestBody Address address) {
        if (address.getId() == null) {
            address.setUserId(TokenUtils.getCurrentUser().getId());
        }
        return Result.success(addressService.saveOrUpdate(address));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(addressService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(addressService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, TokenUtils.getCurrentUser().getId());
        return Result.success(addressService.list(queryWrapper));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(addressService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Address::getId);



        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Address::getName, keyword);
        }

        Account account = TokenUtils.getCurrentUser();

        if(!StrUtil.equals(account.getRole(),"ROLE_ADMIN")){
            queryWrapper.eq(Address::getUserId, account .getId());
        }

        return Result.success(addressService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

