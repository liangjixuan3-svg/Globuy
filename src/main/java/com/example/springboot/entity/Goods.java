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
@TableName(value = "goods")
public class Goods {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String img;

    private String imgList;

    private Integer typeId;

    private BigDecimal price;

    private BigDecimal rePrice;

    private String content;

    private String place;
    
    private String country;

    private String currency;

    private String shipment;

    private Integer userId;

    private Integer num;

    private String status;

    private String quality;

    private String date;

    @TableField(exist = false)
    private Boolean isCollected;

    @TableField(exist = false)
    private String school;

    @TableField(exist = false)
    private Boolean isAuth;
}
