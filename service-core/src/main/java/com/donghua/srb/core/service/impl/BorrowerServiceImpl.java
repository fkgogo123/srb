package com.donghua.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donghua.srb.core.enums.BorrowerStatusEnum;
import com.donghua.srb.core.enums.IntegralEnum;
import com.donghua.srb.core.mapper.BorrowerAttachMapper;
import com.donghua.srb.core.mapper.BorrowerMapper;
import com.donghua.srb.core.mapper.UserInfoMapper;
import com.donghua.srb.core.mapper.UserIntegralMapper;
import com.donghua.srb.core.pojo.entity.Borrower;
import com.donghua.srb.core.pojo.entity.BorrowerAttach;
import com.donghua.srb.core.pojo.entity.UserInfo;
import com.donghua.srb.core.pojo.entity.UserIntegral;
import com.donghua.srb.core.pojo.vo.BorrowerApprovalVO;
import com.donghua.srb.core.pojo.vo.BorrowerAttachVO;
import com.donghua.srb.core.pojo.vo.BorrowerDetailVO;
import com.donghua.srb.core.pojo.vo.BorrowerVO;
import com.donghua.srb.core.service.BorrowerAttachService;
import com.donghua.srb.core.service.BorrowerService;
import com.donghua.srb.core.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerAttachService borrowerAttachService;

    @Resource
    private UserIntegralMapper userIntegralMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerByUserId(BorrowerVO borrowerVO, Long userId) {

        // 获取用户基本信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus()); // 认证中， 信息审核

        baseMapper.insert(borrower);
        
        // 保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        // user_info中也有一个借款人状态字段
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus()); // 认证中
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);

        if (objects.size() == 0) {
            // 用户还未提交基本信息，也就没有状态
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }

        Integer status = (Integer) objects.get(0);
        return status;

    }

    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword) {

        if (StringUtils.isBlank(keyword)) {
            return baseMapper.selectPage(pageParam, null);
        }

        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper
                .like("name", keyword)
                .or().like("id_card", keyword)
                .or().like("mobile", keyword)
                .orderByDesc("id");
        return baseMapper.selectPage(pageParam, borrowerQueryWrapper);
    }

    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {

        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();

        Borrower borrower = baseMapper.selectById(id);

        BeanUtils.copyProperties(borrower, borrowerDetailVO);

        borrowerDetailVO.setMarry(borrower.getMarry()? "是" : "否");
        borrowerDetailVO.setSex(borrower.getSex() == 1?"男" : "女");

        borrowerDetailVO.setEducation(dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation()));
        borrowerDetailVO.setIndustry(dictService.getNameByParentDictCodeAndValue("industry", borrower.getIndustry()));
        borrowerDetailVO.setIncome(dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome()));
        borrowerDetailVO.setContactsRelation(dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource()));
        borrowerDetailVO.setReturnSource(dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation()));

        // 借款人状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        List<BorrowerAttachVO> borrowerAttachVOList = borrowerAttachService.selectBorrowerAttachList(borrower.getId());
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {

        //借款人认证状态
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        Borrower borrower = baseMapper.selectById(borrowerId);
        borrower.setStatus(borrowerApprovalVO.getStatus()); // 借款审批，通过/不通过
        baseMapper.updateById(borrower);

        // 获取用户id
        Long userId = borrower.getUserId();

        // 获取用户对象
        UserInfo userInfo = userInfoMapper.selectById(userId);

        // 积分累加
        int curIntegral = userInfo.getIntegral() + borrowerApprovalVO.getInfoIntegral();

        // 计算积分
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral()); //基本信息积分
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);

        // 身份证积分
        if (borrowerApprovalVO.getIsCarOk()) {
            curIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral()); //基本信息积分
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        // 房产积分
        if (borrowerApprovalVO.getIsHouseOk()) {
            curIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral()); //基本信息积分
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        // 车辆积分
        if (borrowerApprovalVO.getIsCarOk()) {
            curIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral()); //基本信息积分
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        // 更新user_info总积分
        userInfo.setIntegral(curIntegral);
        // 修改审核状态
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus()); // 借款审批，通过/不通过

        userInfoMapper.updateById(userInfo);

    }


}
