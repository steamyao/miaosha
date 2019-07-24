package com.steamyao.miaosha.service;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.service.model.OrderModel;

/**
 * @Package com.steamyao.miaosha.service
 * @date 2019/7/23 11:03
 * @description
 */
public interface OrderService {

    //使用 ：1.通过前段传递的 promoId,下单接口内校验是否属于对应商品，且活动开始
    //2.直接在下单接口判断对应商品是否存在秒杀活动，若存在直接以秒杀活动下单
    OrderModel creatOrder(Integer userId,Integer itemId,Integer promoId,Integer amount) throws BussinessException;
}
