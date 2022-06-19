package com.donghua.srb.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

// 服务消费端，远程调用注解
@EnableFeignClients
@SpringBootApplication
@ComponentScan({"com.donghua.srb", "com.donghua.common"})
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class, args);
    }
}
