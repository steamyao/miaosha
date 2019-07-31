package com.steamyao.miaosha.service.Impl;

import com.steamyao.miaosha.dao.UserDOMapper;
import com.steamyao.miaosha.dao.UserPasswordDOMapper;
import com.steamyao.miaosha.dataobject.UserDO;
import com.steamyao.miaosha.dataobject.UserPasswordDO;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.error.EmBussinessError;
import com.steamyao.miaosha.service.UserService;
import com.steamyao.miaosha.service.model.UserModel;
import com.steamyao.miaosha.validator.ValidationResult;
import com.steamyao.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @Package com.steamyao.miaosha.service.Impl
 * @date 2019/7/3 18:06
 * @description
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null){
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_" + id);
        if(userModel == null){
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_" + id,userModel);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);

        };
        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BussinessException {
        if(userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                  ||userModel.getAge()==null
//                  ||userModel.getGender()==null
//                  ||StringUtils.isEmpty(userModel.getEncrptPassword())
//                  ||StringUtils.isEmpty(userModel.getTelephone())){
//            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult validaResult = validator.validate(userModel);
        if(validaResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validaResult.getErrMsg());

        }

        //实现Model --> dataobject
        UserDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException e){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"手机号已经被注册过");
        }

        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertPasswordDOFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;

    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BussinessException {
        //根据手机号获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO==null){
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);
        //进行密码校验
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }

        return userModel;

    }

    public UserPasswordDO convertPasswordDOFromModel(UserModel userModel){
        if (userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }


    public UserDO convertFromModel(UserModel userModel){
        if (userModel==null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        //拷贝相同的属性
        BeanUtils.copyProperties(userDO,userModel);

        if(userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;

    }
}
