package com.donghua.srb.core;

import com.donghua.srb.core.mapper.DictMapper;
import com.donghua.srb.core.pojo.entity.Dict;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTests {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DictMapper dictMapper;
    @Test
    public void saveDict(){
        Dict dict = dictMapper.selectById(1);
        //向数据库中存储string类型的键值对, 过期时间5分钟
        redisTemplate.opsForValue().set("dict", dict, 5, TimeUnit.MINUTES);
        System.out.println(redisTemplate.opsForValue().get("dict"));
    }

    @Test
    public void getDict(){
        String codeGen = (String)redisTemplate.opsForValue().get("srb:sms:code:18016328020");
        // Dict dict = (Dict)redisTemplate.opsForValue().get("dict");
        System.out.println(codeGen);
    }

    @Test
    public void contextLoads() {
        //测试连接redis保存字符串
        redisTemplate.opsForValue().set("testkey","123456789");
        System.out.println("获得redis，存放键testkey的值："+redisTemplate.opsForValue().get("testkey"));
    }
}
