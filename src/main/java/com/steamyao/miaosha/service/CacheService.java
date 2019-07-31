package com.steamyao.miaosha.service;

/**
 * @Package com.steamyao.miaosha.service
 * @date 2019/7/27 14:31
 * @description  本地缓存
 */

public interface CacheService {

    //设置本地缓存
    void setCommonCache(String key,Object value);

    //获取本地缓存
    Object getFormCommonCache(String key);


}
