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
@TableName(value = "notice")
public class Notice  {

    //解决主键自增问题
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String name;

    private String info;

    private String time;

}
