package com.donghua.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.donghua.srb.core.pojo.bo.TransFlowBO;
import com.donghua.srb.core.pojo.entity.TransFlow;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface TransFlowService extends IService<TransFlow> {

    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);

    List<TransFlow> selectByUserId(Long userId);
}
