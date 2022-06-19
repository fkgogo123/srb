package com.donghua.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donghua.common.exception.Assert;
import com.donghua.common.result.ResponseEnum;
import com.donghua.srb.core.enums.BorrowInfoStatusEnum;
import com.donghua.srb.core.enums.BorrowerStatusEnum;
import com.donghua.srb.core.enums.UserBindEnum;
import com.donghua.srb.core.mapper.BorrowInfoMapper;
import com.donghua.srb.core.mapper.BorrowerMapper;
import com.donghua.srb.core.mapper.IntegralGradeMapper;
import com.donghua.srb.core.mapper.UserInfoMapper;
import com.donghua.srb.core.pojo.entity.BorrowInfo;
import com.donghua.srb.core.pojo.entity.Borrower;
import com.donghua.srb.core.pojo.entity.IntegralGrade;
import com.donghua.srb.core.pojo.entity.UserInfo;
import com.donghua.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.donghua.srb.core.pojo.vo.BorrowerDetailVO;
import com.donghua.srb.core.service.BorrowInfoService;
import com.donghua.srb.core.service.BorrowerService;
import com.donghua.srb.core.service.DictService;
import com.donghua.srb.core.service.LendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        // 在user_info表中获取用户积分
        // 在积分表中查找最大额度

        UserInfo userInfo = userInfoMapper.selectById(userId);
        Integer integral = userInfo.getIntegral();

        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper
                .le("integral_start", integral)
                .ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }
        return integralGrade.getBorrowAmount();

    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        //获取userInfo的用户数据
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //判断用户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);

        //判断用户信息是否审批通过
        Assert.isTrue(
                userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        //判断借款额度是否足够
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        //存储数据
        borrowInfo.setUserId(userId);
        //百分比转成小数
        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus()); // 借款审核
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper
                .select("status")
                .eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);
        if (objects.size() == 0) {
            // 借款人尚未提交借款申请
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        Integer status = (Integer) objects.get(0);
        return status;
    }

    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();
        borrowInfoList.forEach(borrowInfo -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            // String period = dictService.getNameByParentDictCodeAndValue("period", borrowInfo.getPeriod());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());

            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);

        });

        return borrowInfoList;

    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        // 借款信息：BorrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        // 借款人信息：Borrower (BorrowerDetailVO)
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper
                .eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        //修改借款信息状态
        Long id = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus()); // 更改了状态
        borrowInfo.setBorrowYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
        baseMapper.updateById(borrowInfo);

        //审核通过则创建标的
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus()) {
            // 审核通过，创建新标的
            // 新标的信息从 用户提交表单 和 借款信息中得到
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }
    }
}
