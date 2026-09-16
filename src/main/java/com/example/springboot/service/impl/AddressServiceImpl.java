package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Address;
import com.example.springboot.mapper.AddressMapper;
import com.example.springboot.service.IAddressService;
import org.springframework.stereotype.Service;


/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {


}
