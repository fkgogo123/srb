package com.donghua.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donghua.common.exception.Assert;
import com.donghua.common.result.ResponseEnum;
import com.donghua.srb.core.enums.UserBindEnum;
import com.donghua.srb.core.hfb.FormHelper;
import com.donghua.srb.core.hfb.HfbConst;
import com.donghua.srb.core.hfb.RequestHelper;
import com.donghua.srb.core.mapper.UserBindMapper;
import com.donghua.srb.core.mapper.UserInfoMapper;
import com.donghua.srb.core.pojo.entity.UserBind;
import com.donghua.srb.core.pojo.entity.UserInfo;
import com.donghua.srb.core.pojo.vo.UserBindVO;
import com.donghua.srb.core.service.UserBindService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {
        //查询身份证号码是否绑定
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper
                .eq("id_card", userBindVO.getIdCard())
                .ne("user_id", userId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        //USER_BIND_IDCARD_EXIST_ERROR(-301, "身份证号码已绑定"),
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

        // 查询用户是否已经绑定过，防止重复绑定
        userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
        userBind = baseMapper.selectOne(userBindQueryWrapper);
        if (userBind == null) {

            // 插入userbind记录
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus()); // 未绑定
            // userBind.setBindCode(); // 在托管平台的回调接口进行设置
            baseMapper.insert(userBind);
        } else {
            // 用户已存在，更新已绑定记录
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);
        }


        // 组装自动提交表单参数
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard",userBindVO.getIdCard());
        paramMap.put("personalName", userBindVO.getName());
        paramMap.put("bankType", userBindVO.getBankType());
        paramMap.put("bankNo", userBindVO.getBankNo());
        paramMap.put("mobile", userBindVO.getMobile());
        paramMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());

        paramMap.put("sign", RequestHelper.getSign(paramMap)); // 远程接口要求的参数加密方式


        // 组装表单数据
        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {
        String bindCode = (String) paramMap.get("bindCode");
        String agentUserId = (String) paramMap.get("agentUserId");

        // 根据user_id查询user_bind记录
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", agentUserId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);

        // 更新用户绑定表
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);

        // 更新user_info表
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfoMapper.updateById(userInfo);

    }

    @Override
    public String getBindCodeByUserId(Long userId) {

        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        String bindCode = userBind.getBindCode();
        return bindCode;
    }
}
