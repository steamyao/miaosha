package com.steamyao.miaosha.web;

/**
 * @Package com.steamyao.miaosha.web
 * @date 2019/7/3 20:02
 * @description
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义 exceptionHandle 处理未被 controller 处理的异常
    //指定处理的异常类型 设置状态
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public Object handlerException(HttpServletRequest request, Exception ex){
//        Map<String,Object> responseDate = new HashMap<>();
//        if(ex instanceof BussinessException){
//            BussinessException bussinessException = (BussinessException)ex;
//            responseDate.put("errCode",bussinessException.getErrCode());
//            responseDate.put("errMsg",bussinessException.getErrMsg());
//        }else {
//            responseDate.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
//            responseDate.put("errMsg",EmBussinessError.UNKNOWN_ERROR.getErrMsg());
//            ex.printStackTrace();
//        }
//
//        return CommonReturnType.creat("fail",responseDate);
//
//    }
}
