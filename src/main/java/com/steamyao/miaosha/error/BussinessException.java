package com.steamyao.miaosha.error;

/**
 * @Package com.steamyao.miaosha.error
 * @date 2019/7/3 19:18
 * @description
 */

//包装器业务异常实现
public class BussinessException extends Exception implements CommonError {

    private CommonError commonError;

    //直接接收EmBussinessError 用于构造业务异常
    public BussinessException(CommonError commonError){
        super();
        this.commonError = commonError;
    }

    //接收自定义的 errMsg 异常
    public BussinessException(CommonError commonError,String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);

    }

    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
