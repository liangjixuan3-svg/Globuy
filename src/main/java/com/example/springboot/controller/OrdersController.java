package com.example.springboot.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Address;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Orders;
import com.example.springboot.service.IAddressService;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IAddressService addressService;



    @PostMapping
    public Result save(@RequestBody Orders orders) {
        if(orders.getId() == null){
            orders.setNo(DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6));
            Goods goods = goodsService.getById(orders.getItemId());
            orders.setItemName(goods.getName());
            orders.setItemImg(goods.getImg());
            orders.setFromId(goods.getUserId());
            orders.setToId(TokenUtils.getCurrentUser().getId());
            orders.setPrice(goods.getPrice());
            orders.setTime(DateUtil.now());
            orders.setStatus("待支付");
            Address address = addressService.getById(orders.getAddressId());
            orders.setAddress(address.getAddress());
            orders.setInfo(address.getInfo());
            orders.setName(address.getName());
            orders.setPhone(address.getPhone());

            //虽然还没付款，但要将商品状态锁定住，防止其他用户购买
            goods.setStatus("已售出");
            //根据主键生成单条数据的更新SQL语句，仅更新对象中已修改的字段
            goodsService.updateById(goods);
        }

        ordersService.saveOrUpdate(orders);
        return Result.success(orders);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(ordersService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(ordersService.removeByIds(ids));
    }

    @GetMapping("/user/{id}")
    public Result user(@PathVariable Integer id) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getFromId, id);
        wrapper.isNotNull(Orders::getToRate);
        wrapper.isNotNull(Orders::getToReview);
        return Result.success(ordersService.list(wrapper));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(ordersService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(ordersService.getById(id));
    }

    @GetMapping("/pay/{id}")
    public Result pay(@PathVariable Integer id) {
        Orders orders = ordersService.getById(id);
        orders.setStatus("待发货");
        ordersService.updateById(orders);
        return Result.success();
    }

    @GetMapping("/cancel/{id}")
    public Result cancel(@PathVariable Integer id) {
        Orders orders = ordersService.getById(id);
        orders.setStatus("交易关闭");
        ordersService.updateById(orders);

        //将商品状态解锁
        Goods goods = goodsService.getById(orders.getItemId());
        goods.setStatus("已上架");
        goodsService.updateById(goods);
        return Result.success();
    }

    @PostMapping("/shipment")
    public Result shipment(@RequestBody Orders orders) {
        Orders dbOrders = ordersService.getById(orders.getId());
        dbOrders.setStatus("待收货");
        dbOrders.setCarrier(orders.getCarrier());
        dbOrders.setTrackingNo(orders.getTrackingNo());
        dbOrders.setDeliveryTime(DateUtil.now());
        ordersService.updateById(dbOrders);
        return Result.success();
    }

    @GetMapping("/receipt/{id}")
    public Result receipt(@PathVariable Integer id) {
        Orders orders = ordersService.getById(id);
        orders.setStatus("交易完成");
        ordersService.updateById(orders);
        return Result.success();
    }

    @GetMapping("/front/page")
    public Result findFrontPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam String status,
                           @RequestParam String flag,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getId);

        if (!StrUtil.equals(status,"全部")){
            queryWrapper.eq(Orders::getStatus,status);
        }

        Account account = TokenUtils.getCurrentUser();

        if(StrUtil.equals(flag,"我卖出的")){
            queryWrapper.eq(Orders::getFromId,account.getId());
        }

        if(StrUtil.equals(flag,"我买到的")){
            queryWrapper.eq(Orders::getToId,account.getId());
        }

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Orders::getNo, keyword);
        }

        return Result.success(ordersService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Orders::getNo, keyword);
        }

        return Result.success(ordersService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

