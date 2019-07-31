package com.steamyao.miaosha.service.Impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.steamyao.miaosha.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @Package com.steamyao.miaosha.service.Impl
 * @date 2019/7/27 14:38
 * @description
 */
@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String,Object> cache = null;

    @PostConstruct
    private void init(){
        cache = CacheBuilder.newBuilder()
                //设置容器的初始容量
                .initialCapacity(10)
                //设置缓存中最大可以写入100个key，超过之后按照LRU算法抛弃
                .maximumSize(100)
                //设置缓存过期时间
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    @Override
    public void setCommonCache(String key, Object value) {
        cache.put(key,value);
    }

    @Override
    public Object getFormCommonCache(String key) {
        return cache.getIfPresent(key);
    }
}
