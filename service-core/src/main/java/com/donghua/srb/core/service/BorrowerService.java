package com.donghua.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donghua.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.donghua.srb.core.pojo.vo.BorrowerApprovalVO;
import com.donghua.srb.core.pojo.vo.BorrowerDetailVO;
import com.donghua.srb.core.pojo.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerByUserId(BorrowerVO borrowerVO, Long userId);

    Integer getStatusByUserId(Long userId);

    IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword);

    BorrowerDetailVO getBorrowerDetailVOById(Long id);

    void approval(BorrowerApprovalVO borrowerApprovalVO);
}
