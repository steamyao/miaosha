package com.steamyao.miaosha.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Package com.steamyao.miaosha.validator
 * @date 2019/7/6 19:28
 * @description
 */
public class ValidationResult {
    //检验结果是否有错
    private boolean hasErrors = false;

    //存放错误信息的Map
    private Map<String,String> erroeMsgMap = new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErroeMsgMap() {
        return erroeMsgMap;
    }

    public void setErroeMsgMap(Map<String, String> erroeMsgMap) {
        this.erroeMsgMap = erroeMsgMap;
    }

    //返回格式化的错误信息
    public String getErrMsg(){
      return  StringUtils.join(erroeMsgMap.values().toArray(),",");
    }
}
