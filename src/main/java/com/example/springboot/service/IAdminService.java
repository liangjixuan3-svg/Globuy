package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Admin;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IAdminService extends IService<Admin> {

    Account login(Account account);

    void register(Account account);

    void updatePassword(Account account);

}
