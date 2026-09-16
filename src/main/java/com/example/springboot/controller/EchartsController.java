package com.example.springboot.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.springboot.common.Result;

import com.example.springboot.entity.Type;
import com.example.springboot.entity.Goods;
import com.example.springboot.service.ITypeService;
import com.example.springboot.service.IGoodsService;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Resource
    private ITypeService typeService;
    @Resource
    private IGoodsService goodsService;

    @GetMapping("/count")
    public Result count(){

        List<Type> typeList = typeService.list();

        List<Goods> goodsList = goodsService.list();

        JSONArray array = new JSONArray();

        for (Type type : typeList) {
            int count = 0;
            for (Goods goods : goodsList) {
                if(ObjectUtil.equals(goods.getTypeId(),type.getId())){
                    count++;
                }
            }
            JSONObject object = new JSONObject();
            object.set("name",type.getName());
            object.set("value",count);
            array.add(object);
        }

        return Result.success(array);
    }

}
