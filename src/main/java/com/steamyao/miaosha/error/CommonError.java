package com.steamyao.miaosha.error;

/**
 * @Package com.steamyao.miaosha.error
 * @date 2019/7/3 18:56
 * @description
 */
public interface CommonError {
    int getErrCode();
    String getErrMsg();
    CommonError setErrMsg(String errMsg);

}
