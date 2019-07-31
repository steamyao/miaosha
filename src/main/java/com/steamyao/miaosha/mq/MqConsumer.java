package com.steamyao.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.steamyao.miaosha.dao.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @Package com.steamyao.miaosha.mq
 * @date 2019/7/30 16:02
 * @description
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String addr;

    @Value("${mq.topicname}")
    private String topName;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;


    @PostConstruct
    private void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_grop");
        consumer.setNamesrvAddr(addr);
        consumer.subscribe(topName,"*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //减商品库存
                Message message = list.get(0);
                String msg = new String(message.getBody());
                Map<String,Object> map = JSON.parseObject(msg,Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");

                itemStockDOMapper.decreaseStock(itemId,amount);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
    }
}
