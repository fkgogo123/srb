package com.donghua.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donghua.srb.core.listener.ExcelDictDTOListener;
import com.donghua.srb.core.mapper.DictMapper;
import com.donghua.srb.core.pojo.dto.ExcelDictDTO;
import com.donghua.srb.core.pojo.entity.Dict;
import com.donghua.srb.core.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 从外部Excel表中导入数据。
     * 往数据库中插入数据记录的过程在listener中执行
     * @param inputStream
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    /**
     * 查询数据库Dict类型，并转换成Excel类型，用于导出Excel表
     * @return
     */
    @Override
    public List<ExcelDictDTO> listDictData() {
        // 数据库中查找出来的是Dict类型
        List<Dict> dictList = baseMapper.selectList(null);
        // 写入到Excel中的是ExcelDictDTO类型。
        ArrayList<ExcelDictDTO> excelDictDtoList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDtoList.add(excelDictDTO);
        });
        return excelDictDtoList;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {

        /**
         * 基于 Redis 的优化
         * 首先查询Redis中有没有数据列表，有则返回
         * 没有则访问数据库，并将数据列表存入Redis中
         *
         * 问题：导入数据时，应该将Redis清空
         */
        List<Dict> dictList;

        // 尝试从Redis中取值，服务器出现异常也不要停止，还可以从数据库中取值
        try {
            dictList  = (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);
            if (dictList != null) {
                log.info("从Redis中获取数据列表");
                return dictList;
            }
        } catch (Exception e) {
            log.error("Redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }

        log.info("从数据库中获取数据列表");
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", parentId);
        dictList = baseMapper.selectList(dictQueryWrapper);

        dictList.forEach(dict -> {
            // 判断当前结点是否有子结点，即子结点的parent_id是当前结点的id
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });

        // 将数据存入Redis
        try {
            log.info("将数据存入Redis");
            redisTemplate.opsForValue().set("srb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }

        return dictList;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {

        // 根据dictCode 找到id
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        // 根据id找到子项
        return this.listByParentId(dict.getId());
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict parentDict = baseMapper.selectOne(dictQueryWrapper);
        if (parentDict == null) return "";

        dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper
                .eq("parent_id", parentDict.getId())
                .eq("value", value);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        if (dict == null) return "";

        return dict.getName();

    }

    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        Integer integer = baseMapper.selectCount(dictQueryWrapper);
        return integer > 0;
    }

}
