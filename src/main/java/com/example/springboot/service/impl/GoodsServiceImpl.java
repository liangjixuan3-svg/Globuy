package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Goods;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.service.IGoodsService;
import org.springframework.stereotype.Service;


/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {


}
