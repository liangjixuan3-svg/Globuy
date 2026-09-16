package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springboot.common.Constants;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Admin;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;


/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Resource
    private AdminMapper adminMapper;

    @Override
    public Account login(Account account) {
        Admin one = getOne(Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, account.getUsername()).eq(Admin::getPassword, account.getPassword()));
        if (one != null) {
            String role = "ROLE_ADMIN";
            BeanUtils.copyProperties(one,account);
            String token = TokenUtils.createToken( one.getId() + "-" + role, account.getPassword());
            account.setToken(token);
            account.setRole(role);
            account.setPassword(null);
            return account;
        } else {
            throw new ServiceException(Constants.CODE_605, "用户名或密码错误");
        }
    }

    @Override
    public void register(Account account) {
        Admin one = getOne(Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, account.getUsername()));
        if (one == null) {
            one = new Admin();
            BeanUtils.copyProperties(account, one);
            save(one);
        } else {
            throw new ServiceException(Constants.CODE_605, "用户已存在");
        }
    }

    @Override
    public void updatePassword(Account account) {
        LambdaUpdateWrapper<Admin> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Admin::getUsername, account.getUsername());
        wrapper.eq(Admin::getPassword, account.getPassword());
        wrapper.set(Admin::getPassword, account.getNewPassword());
        // 执行更新操作
        int updateCount = adminMapper.update(null, wrapper);
        // 检查更新结果
        if (updateCount == 0) {
            throw new ServiceException(Constants.CODE_605, "原密码输入错误，请检查后再试！");
        }
    }

}
