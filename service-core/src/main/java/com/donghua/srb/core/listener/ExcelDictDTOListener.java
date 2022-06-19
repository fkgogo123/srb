package com.donghua.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.donghua.srb.core.mapper.DictMapper;
import com.donghua.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    private DictMapper dictMapper;

    // 数据列表
    List<ExcelDictDTO> list = new ArrayList<>();
    // 批量存储大小
    private static final int BATCH_COUNT = 5;

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext analysisContext) {
        log.info("解析到一条记录: {}", data);

        list.add(data);

        if (list.size() >= BATCH_COUNT) {
            // 调用mapper的save方法
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 最后的不足一个批次的数据
        saveData();
        log.info("所有数据解析完成");
    }

    private void saveData() {
        log.info("{} 条数据被存储到数据库", list.size());
        dictMapper.insertBatch(list);
        log.info("{} 条数据被存储到数据库", list.size());
    }
}
