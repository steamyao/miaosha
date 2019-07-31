package com.steamyao.miaosha.service;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.service.model.ItemModel;

import java.util.List;

/**
 * @Package com.steamyao.miaosha.service
 * @date 2019/7/22 15:18
 * @description
 */
public interface ItemService {

    //创建商品
    ItemModel creatItemModel(ItemModel itemModel) throws BussinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品查询
    ItemModel getItemById(Integer id);

    //item promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //商品库存减少
    boolean descStock(Integer itemId,Integer amount);

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount);
}
