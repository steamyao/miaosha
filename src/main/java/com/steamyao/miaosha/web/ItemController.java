package com.steamyao.miaosha.web;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.response.CommonReturnType;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.model.ItemModel;
import com.steamyao.miaosha.web.viewObject.ItemVo;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Package com.steamyao.miaosha.web
 * @date 2019/7/22 16:09
 * @description
 */
@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials="true",allowedHeaders="*")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/creat",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType creatItem(@RequestParam("title")String title,
                                      @RequestParam("price")BigDecimal price,
                                      @RequestParam("stock")Integer stock,
                                      @RequestParam("description")String description,
                                      @RequestParam("imgUrl")String imgUrl) throws BussinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setTitle(title);

        ItemModel returnItemModel = itemService.creatItemModel(itemModel);
        ItemVo itemVo = this.convertVoFromModel(returnItemModel);


        return CommonReturnType.creat(itemVo);

    }

    //获取商品详情页
    @RequestMapping(value = "/get",method ={RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id")Integer id){
        ItemModel itemModel = itemService.getItemById(id);
        ItemVo itemVo = this.convertVoFromModel(itemModel);

        return CommonReturnType.creat(itemVo);
    }


    @RequestMapping(value = "/list",method ={RequestMethod.GET})
    @ResponseBody
    public CommonReturnType itemList(){
        List<ItemModel> itemModelList = itemService.listItem();

        //使用steam 的api 将 ModelList 转化为 VoList
        List<ItemVo> itemVoList = itemModelList.stream().map(itemModel -> {
            ItemVo itemVo = this.convertVoFromModel(itemModel);
            return itemVo;
        }).collect(Collectors.toList());

        return CommonReturnType.creat(itemVoList);
    }

    private ItemVo convertVoFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVo itemVo = new ItemVo();
        BeanUtils.copyProperties(itemModel,itemVo);
        if(itemModel.getPromoModel()!=null){
           itemVo.setPromoStatus(itemModel.getPromoModel().getStatus());
           itemVo.setPromoId(itemModel.getPromoModel().getId());
           itemVo.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
           itemVo.setStartTime(itemModel.getPromoModel().getStartTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
           itemVo.setEndTime(itemModel.getPromoModel().getEndTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));

        }else {
            //没有秒杀活动
            itemVo.setPromoStatus(0);
        }
        return itemVo;
    }
}
