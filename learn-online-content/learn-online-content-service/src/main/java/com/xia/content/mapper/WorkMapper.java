package com.xia.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.content.model.po.WorkInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkMapper extends BaseMapper<WorkInfo> {
}