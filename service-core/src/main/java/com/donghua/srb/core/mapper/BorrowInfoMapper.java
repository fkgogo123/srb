package com.donghua.srb.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.donghua.srb.core.pojo.entity.BorrowInfo;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

    List<BorrowInfo> selectBorrowInfoList();

}
