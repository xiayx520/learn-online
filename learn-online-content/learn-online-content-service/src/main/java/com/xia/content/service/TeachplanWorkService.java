package com.xia.content.service;

import com.xia.content.model.po.TeachplanWork;
import java.util.List;

public interface TeachplanWorkService {
    /**
     * 绑定作业到课程计划
     * @param workId 作业ID
     * @param teachplanIds 课程计划ID列表
     */
    void bindWorkToTeachplan(Long workId, List<Long> teachplanIds);

    /**
     * 根据作业ID获取绑定的课程计划
     * @param workId 作业ID
     * @return 课程计划列表
     */
    List<TeachplanWork> getTeachplansByWorkId(Long workId);

    /**
     * 解绑作业与课程计划
     * @param workId 作业ID
     * @param teachplanId 课程计划ID
     */
    void unbindWorkFromTeachplan(Long workId, Long teachplanId);
} 