package com.steamyao.miaosha.service;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.service.model.UserModel;

/**
 * @Package com.steamyao.miaosha.service
 * @date 2019/7/3 18:05
 * @description
 */
public interface UserService {

    UserModel getUserById(Integer id);

    UserModel getUserByIdInCache(Integer id);


    void register(UserModel userModel) throws BussinessException;

    //用户手机号  加密后的密码
    UserModel validateLogin(String telphone,String encrptPassword) throws BussinessException;
}
