package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "type")
public class Type {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String name;

    private String info;

    private String icon;

    private String img;

    private Boolean status;

    //该字段在表中不存在，仅用于查询时返回
    @TableField(exist = false)
    private List<Goods> goodsList;

}
