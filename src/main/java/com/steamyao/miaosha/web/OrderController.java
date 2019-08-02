package com.steamyao.miaosha.web;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.mq.MqProducer;
import com.steamyao.miaosha.response.CommonReturnType;
import com.steamyao.miaosha.service.ItemService;
import com.steamyao.miaosha.service.OrderService;
import com.steamyao.miaosha.service.PromoService;
import com.steamyao.miaosha.service.model.UserModel;
import com.steamyao.miaosha.utils.ImgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer producer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;


    private ExecutorService executorService;

    @PostConstruct
    public void init(){
        //创建线程池，可以用作泄洪队列,拥塞窗口为10
        executorService = Executors.newFixedThreadPool(10);
    }

    @RequestMapping(value = "/verifyCode",method ={RequestMethod.GET})
    @ResponseBody
    public  void generateVerifyCode(HttpServletResponse response) throws BussinessException, IOException {
        //前端传过来的token host路径
        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
        }
        //获取用户信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null ){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
        }

        //获取验证码，存入redis，并返回前端
        Map<String,Object> map = ImgUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("veryfy_code"+userModel.getId(),map.get("code"));
        redisTemplate.expire("veryfy_code"+userModel.getId(),5, TimeUnit.MINUTES);
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }

    @RequestMapping(value = "/generateToken",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatePromoToken(@RequestParam("itemId")Integer itemId,
                                               @RequestParam(value = "promoId")Integer promoId,
                                               @RequestParam(value = "verifyCode")String verifyCode) throws BussinessException{

        //前端传过来的token host路径
        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
        }

        //获取用户信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null ){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
        }

        //校验验证码是否正确
        if(StringUtils.isEmpty(verifyCode)){
            throw  new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"验证码不正确");
        }
        String inRedisVerifyCode =(String) redisTemplate.opsForValue().get("veryfy_code" + userModel.getId());
        if(!org.apache.commons.lang3.StringUtils.equalsIgnoreCase(verifyCode,inRedisVerifyCode)){
            throw  new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"验证码不正确");
        }


        //生成秒杀令牌
        String promoToken = promoService.generatrSecondKillToken(promoId, userModel.getId(), itemId);

        if (promoToken == null){
            throw  new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"获取秒杀令牌失败");
        }


        return CommonReturnType.creat(promoToken);
    }




    @RequestMapping(value = "/creatOrder",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType creatOrder(@RequestParam("itemId")Integer itemId,
                                       @RequestParam("amount")Integer amount,
                                       @RequestParam(value = "promoId",required = false)Integer promoId,
                                       @RequestParam(value = "promoToken",required = false)String promoToken) throws BussinessException {

       //前端传过来的token host路径
       String token =  httpServletRequest.getParameterMap().get("token")[0];
       if (StringUtils.isEmpty(token)){
           throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
       }

        //获取用户信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null ){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户还未登录");
        }

        //检验前端的秒杀令牌与后台的是否一致
        if(promoId != null){
            String  inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_item_"+itemId+"_user_"+userModel.getId());
            if(inRedisPromoToken ==null ||
                    !org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌错误");
            }

        }


        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //1. 初始化库存流水状态
                String stockLogId = itemService.initStockLog(itemId, amount);


                //2. 发送事务型消息扣减库存
                if (!producer.transactionAsynDescStock(userModel.getId(), promoId, itemId, amount, stockLogId)) {
                    throw new BussinessException(EmBussinessError.UNKNOWN_ERROR, "下单失败");
                }

                return null;
            }
        });

        //等待执行完成
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BussinessException(EmBussinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BussinessException(EmBussinessError.UNKNOWN_ERROR);
        }


        return CommonReturnType.creat(null);
    }
}
