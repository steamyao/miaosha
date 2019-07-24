package com.steamyao.miaosha.web;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.response.CommonReturnType;
import com.steamyao.miaosha.service.OrderService;
import com.steamyao.miaosha.service.model.OrderModel;
import com.steamyao.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @Package com.steamyao.miaosha.web
 * @date 2019/7/23 15:02
 * @description
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials="true",allowedHeaders="*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/creatOrder",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType creatOrder(@RequestParam("itemId")Integer itemId,
                                       @RequestParam("amount")Integer amount,
                                       @RequestParam(value = "promoId",required = false)Integer promoId) throws BussinessException {

        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin==null || !isLogin.booleanValue()){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN);
        }
        //获取用户信息
        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");
        OrderModel orderModel = orderService.creatOrder(userModel.getId(), itemId,promoId,amount);


        return CommonReturnType.creat(orderModel);
    }
}
