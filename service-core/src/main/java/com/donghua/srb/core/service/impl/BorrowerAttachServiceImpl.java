package com.donghua.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donghua.srb.core.pojo.entity.BorrowerAttach;
import com.donghua.srb.core.mapper.BorrowerAttachMapper;
import com.donghua.srb.core.pojo.vo.BorrowerAttachVO;
import com.donghua.srb.core.service.BorrowerAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {

    @Override
    public List<BorrowerAttachVO> selectBorrowerAttachList(Long borrowerId) {

        QueryWrapper<BorrowerAttach> borrowerAttachQueryWrapper = new QueryWrapper<>();
        borrowerAttachQueryWrapper
                .eq("borrower_id", borrowerId);
        List<BorrowerAttach> borrowerAttachList = baseMapper.selectList(borrowerAttachQueryWrapper);
        ArrayList<BorrowerAttachVO> borrowerAttachVOArrayList = new ArrayList<>();

        borrowerAttachList.forEach(borrowerAttach -> {
            BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
            borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());
            borrowerAttachVO.setImageType(borrowerAttach.getImageType());
            borrowerAttachVOArrayList.add(borrowerAttachVO);
        });

        return borrowerAttachVOArrayList;

    }
}
