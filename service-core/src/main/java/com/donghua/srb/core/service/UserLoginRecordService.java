package com.donghua.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.donghua.srb.core.pojo.entity.UserLoginRecord;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface UserLoginRecordService extends IService<UserLoginRecord> {

    List<UserLoginRecord> listTop50(Integer userId);
}
