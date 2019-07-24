package com.steamyao.miaosha.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @Package com.steamyao.miaosha.service.model
 * @date 2019/7/24 8:11
 * @description  秒杀模型
 */
public class PromoModel {

    private Integer id;


    //时间标志位 1还未开始  2进行中  3已经结束
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    //开始时间
    private DateTime startTime;

    //结束时间
    private DateTime endTime;


    //商品ID
    private Integer itemId;

    //商品名称
    private String promoName;

    //秒杀时商品价格
    private BigDecimal promoItemPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }
    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }
}
