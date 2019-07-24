package com.steamyao.miaosha.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * @Package com.steamyao.miaosha.validator
 * @date 2019/7/6 19:35
 * @description
 */
@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

   public ValidationResult validate(Object bean){
       ValidationResult result = new ValidationResult();
       Set<ConstraintViolation<Object>> violationSet = validator.validate(bean);
       if(violationSet.size()>0){
           //有异常
           result.setHasErrors(true);
           violationSet.forEach(violation->{
               String errMsg = violation.getMessage();
               String propertyName = violation.getPropertyPath().toString();
               result.getErroeMsgMap().put(propertyName,errMsg);
           });
       }
       return result;
   }


    //在bean 初始化之后执行
    @Override
    public void afterPropertiesSet() throws Exception {
        //通过工厂初始化方法，使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
