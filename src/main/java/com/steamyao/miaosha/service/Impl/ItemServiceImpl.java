package com.steamyao.miaosha.service.Impl;

import com.steamyao.miaosha.dao.ItemDOMapper;
import com.steamyao.miaosha.dao.ItemStockDOMapper;
import com.steamyao.miaosha.dataobject.ItemDO;
import com.steamyao.miaosha.dataobject.ItemStockDO;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.PromoService;
import com.steamyao.miaosha.service.model.ItemModel;
import com.steamyao.miaosha.service.model.PromoModel;
import com.steamyao.miaosha.validator.ValidationResult;
import com.steamyao.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Package com.steamyao.miaosha.service.Impl
 * @date 2019/7/22 15:22
 * @description
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;


    @Override
    @Transactional
    public ItemModel creatItemModel(ItemModel itemModel) throws BussinessException {
        if(itemModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        //校验参数
        ValidationResult validaResult = validator.validate(itemModel);
        if(validaResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validaResult.getErrMsg());
        }

        //itemModel  -> dataObjecy
        ItemDO itemDO = this.convertItemDOFromModel(itemModel);


        //加入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);


        //返回创建成功的对象
        return this.getItemById(itemDO.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null){
            return null;
        }
        //操作对应的数据
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //dataObject -> model
        ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);


        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoById(itemModel.getId());
        if(promoModel !=null && promoModel.getStatus().intValue()!=3){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean descStock(Integer itemId, Integer amount) {
        int affectRow = itemStockDOMapper.decreaseStock(itemId, amount);
        if(affectRow>0){
            //成功
            return true;
        }
        //失败
        return false;
    }


    @Override
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId,amount);
    }


    private ItemDO convertItemDOFromModel(ItemModel itemModel){
        if (itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromModel(ItemModel itemModel){
        if (itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());

        return itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
