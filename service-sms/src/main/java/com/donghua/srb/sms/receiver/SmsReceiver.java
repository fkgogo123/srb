package com.donghua.srb.sms.receiver;

import com.donghua.srb.base.dto.SmsDTO;
import com.donghua.srb.rabbitutil.constant.MQConst;
import com.donghua.srb.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsReceiver {

    @Resource
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SMS_ITEM, durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
            key = {MQConst.ROUTING_SMS_ITEM}
    ))
    public void send(SmsDTO smsDTO) throws IOException {
        log.info("SmsReceiver 消息监听.....");
        Map<String,Object> param = new HashMap<>();
        param.put("code", smsDTO.getMessage()); // 发送消息
        // param.put("code", "6666"); // 发送数字
        // smsService.send(smsDTO.getMobile(), SmsProperties.TEMPLATE_CODE, param);
        log.info("短信发送成功, " + smsDTO.getMobile() + ", " + smsDTO.getMessage());
    }

}
