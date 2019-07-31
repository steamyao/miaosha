package com.steamyao.miaosha.service;

import com.steamyao.miaosha.service.model.PromoModel;

/**
 * @Package com.steamyao.miaosha.service
 * @date 2019/7/24 8:53
 * @description
 */
public interface PromoService {

    PromoModel getPromoById(Integer itemId);

    //发布库存到redis 缓存
    void publishPromoById(Integer prompId);
}
