package com.donghua.srb.sms.client;

import com.donghua.srb.sms.client.fallback.CoreUserInfoClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-core", fallback = CoreUserInfoClientFallback.class)
// 为客户端指定远程微服务地址, 及熔断降级的处理办法
public interface CoreUserInfoClient {

    // 远程接口
    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable String mobile);

}
