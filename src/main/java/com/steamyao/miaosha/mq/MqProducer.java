package com.steamyao.miaosha.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
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

    @Value("${mq.nameserver.addr}")
    private String addr;

    @Value("${mq.topicname}")
    private String topName;

    @PostConstruct
    private void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_grop");
        producer.setNamesrvAddr(addr);

        producer.start();;
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
