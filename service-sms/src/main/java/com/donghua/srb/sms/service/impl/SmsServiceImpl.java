package com.donghua.srb.sms.service.impl;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.donghua.common.exception.Assert;
import com.donghua.common.exception.BusinessException;
import com.donghua.common.result.ResponseEnum;
import com.donghua.srb.sms.service.SmsService;
import com.donghua.srb.sms.util.SmsProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    @Override
    public void send(String mobile, String templateCode, Map<String, Object> param) {

        // java.util.List<String> args = java.util.Arrays.asList(args_);
        com.aliyun.dysmsapi20170525.Client client = null;
        try {
            client = SmsServiceImpl.createClient(SmsProperties.KEY_ID, SmsProperties.KEY_SECRET);
        } catch (Exception e) {
            log.error("远程连接客户端对象创建失败：" + e.getMessage());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, e);
            // e.printStackTrace();
        }
        Gson gson = new Gson();
        String jsonParam = gson.toJson(param);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(mobile)
                .setSignName(SmsProperties.SIGN_NAME)
                .setTemplateCode(templateCode)
                .setTemplateParam(jsonParam);
        ;
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);

            String code = sendSmsResponse.getBody().code;
            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL", code, ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
            Assert.equals("OK", code, ResponseEnum.ALIYUN_SMS_ERROR);

        } catch (TeaException error) {
            log.error("TeaException阿里云短信发送sdk调用失败：" + error.getCode() + ", " + error.getMessage());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, error);
            // 如有需要，请打印 error
            // com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            log.error("Exception阿里云短信发送sdk调用失败：" + error.getCode() + ", " + error.getMessage());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, error);

            // 如有需要，请打印 error
            // com.aliyun.teautil.Common.assertAsString(error.message);
        }
    }
}
