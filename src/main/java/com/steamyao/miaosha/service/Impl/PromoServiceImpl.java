package com.steamyao.miaosha.service.Impl;

import com.steamyao.miaosha.dao.PromoDOMapper;
import com.steamyao.miaosha.dataobject.PromoDO;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.PromoService;
import com.steamyao.miaosha.service.UserService;
import com.steamyao.miaosha.service.model.ItemModel;
import com.steamyao.miaosha.service.model.PromoModel;
import com.steamyao.miaosha.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private UserService userService;

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
        redisTemplate.expire("promo_item_stock_"+itemModel.getId(),2,TimeUnit.HOURS);

        //设置秒杀大闸为 5倍的库存数量
        redisTemplate.opsForValue().set("promo_door_count_"+prompId,itemModel.getStock()*5);
        redisTemplate.expire("promo_door_count_"+prompId,2,TimeUnit.HOURS);


    }

    @Override
    public String generatrSecondKillToken(Integer promoId,Integer userId,Integer itemId) throws BussinessException {

        //0 查询售罄的表识别
        if(redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            return  null;
        }


        //校验用户信息，商品信息
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
               return null;
        }
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel==null){
            return null;
        }

        //秒杀信息校验
        if(promoId !=null){
            //{1} 校验前端的promoid 是否属于秒杀商品
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
               return null;
            }
            //{2} 校验活动是否开始
            if (itemModel.getPromoModel().getStatus()!=2){
                return null;
            }
        }



//        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
//        PromoModel promoModel = this.convertFromDataObject(promoDO);
        //todo 如果多个秒杀商品的 itemId 一致 怎么解决
        PromoModel promoModel = itemModel.getPromoModel();
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
        if(promoModel.getStatus() != 2){
            return null;
        }

        //秒杀大闸的数量减1
        long num = redisTemplate.opsForValue().decrement("promo_door_count_" + promoId, 1);
        if(num <0){
            return null;
        }

        //生成秒杀令牌
        String token = UUID.randomUUID().toString().replace("-","");

        redisTemplate.opsForValue().set("promo_token_"+promoId+"_item_"+itemId+"_user_"+userId,token);
        redisTemplate.expire("promo_token_"+promoId+"_item_"+itemId+"_user_"+userId,5, TimeUnit.MINUTES);


        return token;
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
