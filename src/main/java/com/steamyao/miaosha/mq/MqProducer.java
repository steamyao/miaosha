package com.steamyao.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.steamyao.miaosha.dao.StockLogDOMapper;
import com.steamyao.miaosha.dataobject.StockLogDO;
import com.steamyao.miaosha.error.BussinessException;
import com.steamyao.miaosha.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Package com.steamyao.miaosha.mq
 * @date 2019/7/30 16:02
 * @description
 */
@Component
public class MqProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;


    @Value("${mq.nameserver.addr}")
    private String addr;

    @Value("${mq.topicname}")
    private String topName;

    @PostConstruct
    private void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_grop");
        producer.setNamesrvAddr(addr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_grop");
        transactionMQProducer.setNamesrvAddr(addr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object args) {
               //真正要做的事
                Map<String,Object> map = (Map<String, Object>) args;
                Integer userId = (Integer) map.get("userId");
                Integer promoId = (Integer) map.get("promoId");
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String)map.get("stockLogId");

                try {
                    orderService.creatOrder(userId,itemId,promoId,amount,stockLogId);
                } catch (BussinessException e) {
                    e.printStackTrace();
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
               //根据库存是否扣减成功，来返回ROLLBACK_MESSAGE  COMMIT_MESSAGE
                String jsonString = new String(messageExt.getBody());
                Map<String,Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer)map.get("amount");
                String stockLogId = (String)map.get("stockLogId");

                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if(stockLogDO == null){
                    return  LocalTransactionState.UNKNOW;
                }
                if(stockLogDO.getStatus().intValue() == 2){
                    return  LocalTransactionState.COMMIT_MESSAGE;
                }else if(stockLogDO.getStatus().intValue() == 1){
                    return  LocalTransactionState.UNKNOW;
                }else
                    return  LocalTransactionState.ROLLBACK_MESSAGE;

            }
        });
    }


    //同步型事务扣减库存
    public boolean transactionAsynDescStock(Integer userId, Integer promoId, Integer itemId, Integer amount,String stockLogId){
        Map<String,Object> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("amount",amount);
        map.put("stockLogId",stockLogId);

        //传递参数
        Map<String,Object> argsMap = new HashMap<>();
        argsMap.put("userId",userId);
        argsMap.put("promoId",promoId);
        argsMap.put("itemId",itemId);
        argsMap.put("amount",amount);
        argsMap.put("stockLogId",stockLogId);

        TransactionSendResult result = null;

        Message message = new Message(topName,"increase",
                JSON.toJSON(map).toString().getBytes(Charset.forName("utf-8")));

        try {
           result =  transactionMQProducer.sendMessageInTransaction(message,argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (result.getLocalTransactionState()==LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }else if(result.getLocalTransactionState()==LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }else {
            return false;
        }
    }



    //同步消息减库存
    public boolean asynaDescStock(Integer itemId, Integer amount)  {
        Map<String,Object> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("amount",amount);
        Message message = new Message(topName,"increase",
                JSON.toJSON(map).toString().getBytes(Charset.forName("utf-8")));

        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



}
