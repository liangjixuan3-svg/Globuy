package com.example.springboot.common;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Orders;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IOrdersService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class OrderTask {

    @Resource
    private IOrdersService ordersService;

    @Resource
    private IGoodsService goodsService;

    /**
     * 每分钟扫描一次待支付订单，检查是否超时（5分钟）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkOrderTimeout() {
        // 查找所有待支付订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, "待支付");
        List<Orders> ordersList = ordersService.list(wrapper);

        Date now = new Date();
        for (Orders order : ordersList) {
            String orderTimeStr = order.getTime();
            try {
                Date orderTime = DateUtil.parse(orderTimeStr);
                // 如果当前时间比订单时间晚 5 分钟以上
                if (DateUtil.betweenMs(orderTime, now) > 5 * 60 * 1000) {
                    // 1. 取消订单
                    order.setStatus("交易关闭");
                    ordersService.updateById(order);

                    // 2. 还原商品状态
                    Goods goods = goodsService.getById(order.getItemId());
                    if (goods != null) {
                        goods.setStatus("已上架");
                        goodsService.updateById(goods);
                    }
                    System.out.println("订单 " + order.getNo() + " 已超时自动取消并还原库存");
                }
            } catch (Exception e) {
                // 解析时间失败则跳过
            }
        }
    }
}
