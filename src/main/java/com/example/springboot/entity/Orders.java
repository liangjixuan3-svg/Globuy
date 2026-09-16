package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;


/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "orders")
public class Orders {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String no;

    private Integer itemId;

    private String itemName;

    private String itemImg;

    private Integer fromId;

    private Integer toId;

    private BigDecimal price;

    private String time;

    private String status;

    private Integer toRate;

    private String toReview;

    private String address;

    private String info;

    private String name;

    private String phone;

    private String carrier;

    private String trackingNo;

    private String deliveryTime;

    @TableField(exist = false)
    private Integer addressId;
}
