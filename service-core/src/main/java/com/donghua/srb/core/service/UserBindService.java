package com.donghua.srb.core.service;

import com.donghua.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.donghua.srb.core.pojo.vo.UserBindVO;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface UserBindService extends IService<UserBind> {

    String commitBindUser(UserBindVO userBindVO, Long userId);

    void notify(Map<String, Object> paramMap);

    String getBindCodeByUserId(Long investUserId);
}
