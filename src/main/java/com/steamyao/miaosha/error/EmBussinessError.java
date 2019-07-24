package com.steamyao.miaosha.error;

/**
 * @Package com.steamyao.miaosha.error
 * @date 2019/7/3 19:00
 * @description
 */
public enum EmBussinessError implements CommonError {
    //10001通用的错误类型
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),


    //20000开头的为用户信息相关的错误
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAIL(20002,"用户手机号或密码不存在"),
    USER_NOT_LOGIN(20003,"用户还未登陆"),


    //30000开头为 交易信息错误
    STOCK_NOT_ENOUGH(30001,"商品库存不足")
    ;

   private EmBussinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
