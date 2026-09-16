package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Chat;
import com.example.springboot.mapper.ChatMapper;
import com.example.springboot.service.IChatService;
import org.springframework.stereotype.Service;


/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements IChatService {


}
