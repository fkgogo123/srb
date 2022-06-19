package com.donghua.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donghua.common.exception.Assert;
import com.donghua.common.result.ResponseEnum;
import com.donghua.srb.base.dto.SmsDTO;
import com.donghua.srb.core.enums.TransTypeEnum;
import com.donghua.srb.core.hfb.FormHelper;
import com.donghua.srb.core.hfb.HfbConst;
import com.donghua.srb.core.hfb.RequestHelper;
import com.donghua.srb.core.mapper.UserAccountMapper;
import com.donghua.srb.core.mapper.UserInfoMapper;
import com.donghua.srb.core.pojo.bo.TransFlowBO;
import com.donghua.srb.core.pojo.entity.UserAccount;
import com.donghua.srb.core.pojo.entity.UserInfo;
import com.donghua.srb.core.service.TransFlowService;
import com.donghua.srb.core.service.UserAccountService;
import com.donghua.srb.core.service.UserBindService;
import com.donghua.srb.core.service.UserInfoService;
import com.donghua.srb.core.util.LendNoUtils;
import com.donghua.srb.rabbitutil.constant.MQConst;
import com.donghua.srb.rabbitutil.service.MQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Slf4j
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private MQService mqService;

    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        // 获取充值人绑定协议号
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();

        //判断账户绑定状态
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigInteger("0"));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());

        paramMap.put("sign", RequestHelper.getSign(paramMap)); // 参数加密，排序，加密

        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
        return formStr;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {

        // 先做幂等性判断.
        /*
        例：
        汇付宝充值成功，尚融宝拿到数据还没来得及同步，未返回success。
        此时网络断开，汇付宝将发起重试，会对账户进行重复充值
         */
        // 如何对接口已经调用过进行判断，判断过则不再处理。
        // 答: 通过交易流水进行判断，bindcode是否已经
        String bindCode = (String) paramMap.get("bindCode");
        String chargeAmt = (String) paramMap.get("chargeAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        String agentBillNo = (String) paramMap.get("agentBillNo"); //商户充值订单号
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if (isSave){
            log.warn("幂等性返回");
            return "success";
        }

        // 账户处理
        //增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值");

        transFlowService.saveTransFlow(transFlowBO);

        // 向 mq 中发消息，给用户发送资金变动短信
        log.info("发消息");
        String mobile = userInfoService.getMobileByBindCode(bindCode);
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMobile(mobile);
        smsDTO.setMessage("充值成功");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS, MQConst.ROUTING_SMS_ITEM, smsDTO);

        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        // 根据user_id查找用户账户
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);
        BigDecimal amount = userAccount.getAmount();
        return amount;
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        // 判断是否已绑定，余额是否充足
        BigDecimal amount = userAccountService.getAccount(userId);
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(), ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);
        // 组装参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        // 构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        log.info("提现成功");
        String agentBillNo = (String) paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) {
            log.info("幂等性返回");
            return;
        }
        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");

        // 根据用户账户修改账户金额
        baseMapper.updateAccount(bindCode, new BigDecimal(fetchAmt).negate(), new BigDecimal(0));

        // 增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "提现"
        );
        transFlowService.saveTransFlow(transFlowBO);
    }
}
