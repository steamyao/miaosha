package com.steamyao.miaosha.web;

import com.alibaba.druid.util.StringUtils;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.response.CommonReturnType;
import com.steamyao.miaosha.service.UserService;
import com.steamyao.miaosha.service.model.UserModel;
import com.steamyao.miaosha.web.viewObject.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @Package com.steamyao.miaosha.web
 * @date 2019/7/3 18:03
 * @description
 */
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials="true",allowedHeaders="*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping("/login")
    @ResponseBody
    public CommonReturnType login(@RequestParam("telphone")String telphone,
                                  @RequestParam("password")String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)
        || org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录校验
        UserModel userModel = userService.validateLogin(telphone,this.EncodeByMD5(password));

        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);


        return CommonReturnType.creat(null);
    }

    //用户注册接口
    @RequestMapping("/register")
    @ResponseBody
    public CommonReturnType register(@RequestParam("telphone")String telphone,
                                     @RequestParam("otpCode")String otpCode,
                                     @RequestParam("name")String name,
                                     @RequestParam("gender")Integer gender,
                                     @RequestParam("age")Integer age,
                                     @RequestParam("password")String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证otpCode 与手机号是否匹配
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册
       UserModel userModel = new UserModel();
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setName(name);
        userModel.setTelephone(telphone);
        userModel.setRegisterMode("通过手机号注册");
        userModel.setEncrptPassword(this.EncodeByMD5(password));

        userService.register(userModel);

        return CommonReturnType.creat(null);
    }

    public String EncodeByMD5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //确认加密方式
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }


    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam("telphone")String telphone){
        //按照一定的规则生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //蒋otp验证码与手机号关联,使用httpsession 绑定otp与手机号
        httpServletRequest.getSession().setAttribute(telphone,otpCode);


        //蒋otp验证码以短信发送给用户 略
        System.out.println("telphone= "+telphone+"  & otpCode "+otpCode);
        return CommonReturnType.creat(null);
    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam("id")Integer id) throws BussinessException {
        UserModel userModel = userService.getUserById(id);

        if(userModel == null){
            throw new BussinessException(EmBussinessError.USER_NOT_EXIST);
        }
        UserVo userVo = convertFromModel(userModel);

        //定义通用的返回类型
        return CommonReturnType.creat(userVo);
    }


    private UserVo convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel,userVo);

        return userVo;
    }


}
