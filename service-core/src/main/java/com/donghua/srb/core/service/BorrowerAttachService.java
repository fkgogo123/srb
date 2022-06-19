package com.donghua.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.donghua.srb.core.pojo.entity.BorrowerAttach;
import com.donghua.srb.core.pojo.vo.BorrowerAttachVO;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {

    List<BorrowerAttachVO> selectBorrowerAttachList(Long borrowerId);
}
