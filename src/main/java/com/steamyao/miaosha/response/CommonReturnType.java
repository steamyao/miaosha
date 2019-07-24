package com.steamyao.miaosha.response;

/**
 * @Package com.steamyao.miaosha.response
 * @date 2019/7/3 18:43
 * @description
 */
public class CommonReturnType {

    private String status;
    private Object data;


    public static CommonReturnType creat(Object data){
        return CommonReturnType.creat("success",data);
    }

    public static CommonReturnType creat(String status, Object data) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(data);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
