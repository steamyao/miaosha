package com.steamyao.miaosha.web;

import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.response.CommonReturnType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Package com.steamyao.miaosha.web
 * @date 2019/7/25 8:48
 * @description  处理404  405 问题
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object doError(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        ex.printStackTrace();
        Map<String, Object> responseDate = new HashMap<>();
        if (ex instanceof BussinessException) {
            BussinessException bussinessException = (BussinessException) ex;
            responseDate.put("errCode", bussinessException.getErrCode());
            responseDate.put("errMsg", bussinessException.getErrMsg());
        } else if (ex instanceof ServletRequestBindingException) {
            responseDate.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
            responseDate.put("errMsg", "绑定路由问题");
        } else if (ex instanceof NoHandlerFoundException) {
            responseDate.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
            responseDate.put("errMsg", "没有找到对应的访问路径");
        } else {
            responseDate.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
            responseDate.put("errMsg", EmBussinessError.UNKNOWN_ERROR.getErrMsg());
        }

        return CommonReturnType.creat("fail", responseDate);
    }
}
