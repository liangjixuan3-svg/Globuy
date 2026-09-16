package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "banner")
public class Banner {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String name;

    private String img;

}
