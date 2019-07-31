package com.steamyao.miaosha.service.Impl;

import com.steamyao.miaosha.dao.OrderDOMapper;
import com.steamyao.miaosha.dao.SequenceDOMapper;
import com.steamyao.miaosha.dataobject.OrderDO;
import com.steamyao.miaosha.dataobject.SequenceDO;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.OrderService;
import com.steamyao.miaosha.service.UserService;
import com.steamyao.miaosha.service.model.ItemModel;
import com.steamyao.miaosha.service.model.OrderModel;
import com.steamyao.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Package com.steamyao.miaosha.service.Impl
 * @date 2019/7/23 11:04
 * @description
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel creatOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BussinessException {
        //1.校验参数的合法性
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"用户不存在");
        }
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel==null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品不存在");
        }
        if(amount<0 || amount>99){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品数量不合法");
        }

        //秒杀信息校验
        if(promoId !=null){
            //{1} 校验前端的promoid 是否属于秒杀商品
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }
            //{2} 校验活动是否开始
            if (itemModel.getPromoModel().getStatus()!=2){
                throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"秒杀还未开始活动");
            }
        }


        //2.下单减库存
        boolean result = itemService.descStock(itemId, amount);
        if(!result){
            throw new BussinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));


        //生成ID
        orderModel.setId(generateOrderNp());
        //model -> dataObject
        OrderDO orderDO = this.convertFromModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //增加销量
        itemService.increaseSales(itemId,amount);

        //4.返回前端
        return orderModel;
    }

    //强制开启一个新的事务，成功与否都将进行提交
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNp(){
        //订单序列号16为
        StringBuffer stringBuffer = new StringBuffer();
        //1. 前8位为 年月日
        LocalDateTime dateTime = LocalDateTime.now();
        String data = dateTime.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuffer.append(data);


        //2. 中间6位为自增寻列
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6-sequenceStr.length(); i++) {
           stringBuffer.append("0");
        }
        stringBuffer.append(sequenceStr);


        //3. 最后两位为分库分表 暂时写死
        stringBuffer.append("00");

        return stringBuffer.toString();
    }

    private OrderDO convertFromModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
