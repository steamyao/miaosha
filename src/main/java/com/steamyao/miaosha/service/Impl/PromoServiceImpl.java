package com.steamyao.miaosha.service.Impl;

import com.steamyao.miaosha.dao.PromoDOMapper;
import com.steamyao.miaosha.dataobject.PromoDO;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.PromoService;
import com.steamyao.miaosha.service.model.ItemModel;
import com.steamyao.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Package com.steamyao.miaosha.service.Impl
 * @date 2019/7/24 8:54
 * @description
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    @Override
    public PromoModel getPromoById(Integer itemId) {

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = this.convertFromDataObject(promoDO);

        if (promoModel==null){
            return null;
        }

        //判断秒杀时间 设置标志位
        if(promoModel.getStartTime().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndTime().isBeforeNow()){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publishPromoById(Integer prompId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(prompId);
        if(promoDO.getItemId()==null || promoDO.getItemId().intValue()==0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());

        //同步库存到缓存
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());

    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartTime(new DateTime(promoDO.getStartTime()));
        promoModel.setEndTime(new DateTime(promoDO.getEndTime()));
        return promoModel;
    }
}
