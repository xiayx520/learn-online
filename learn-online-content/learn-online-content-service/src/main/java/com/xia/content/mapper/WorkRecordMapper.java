package com.xia.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.content.model.po.WorkRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业记录Mapper接口
 */
@Mapper
public interface WorkRecordMapper extends BaseMapper<WorkRecord> {
}