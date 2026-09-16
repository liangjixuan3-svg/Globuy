package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    Account login(Account account);

    void register(Account account);

    void updatePassword(Account account);

}
