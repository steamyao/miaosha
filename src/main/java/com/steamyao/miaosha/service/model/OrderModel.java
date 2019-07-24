package com.steamyao.miaosha.service.model;

import java.math.BigDecimal;

/**
 * @Package com.steamyao.miaosha.service.model
 * @date 2019/7/23 10:47
 * @description
 */
public class OrderModel {

    private String id;

    //用户ID
    private Integer userId;
    //商品ID
    private Integer itemId;

    //若不为0，表示以秒杀方式下单
    private Integer promoId;
    //购买商品数量
    private Integer amount;
    //购买商品单价  若promoId不为0，表示秒杀价格
    private BigDecimal itemPrice;
    //购买商品总价  若promoId不为0，表示秒杀价格
    private BigDecimal orderPrice;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }
}
