package com.xia.content.service;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.QueryWorkRecordParamsDto;
import com.xia.content.model.dto.WorkGradeDTO;
import com.xia.content.model.dto.WorkInfoDto;
import com.xia.content.model.dto.WorkSubmitDTO;
import com.xia.content.model.po.WorkInfo;
import com.xia.content.model.po.WorkRecord;
import com.xia.content.model.po.WorkGrade;
import com.xia.content.model.dto.IWorkRecOverallDTO;

public interface WorkService {

    /**
     * 创建或更新作业
     * @param workInfoDto 作业信息
     * @return 作业信息
     */
    WorkInfo saveWork(WorkInfoDto workInfoDto);

    /**
     * 提交作业
     * @param workSubmitDTO 作业提交信息
     * @return 作业记录信息
     */
    WorkRecord submitWork(WorkSubmitDTO workSubmitDTO);

    /**
     * 查询作业提交记录列表
     * @param pageParams 分页参数
     * @param queryWorkRecordParamsDto 查询参数
     * @return 分页结果
     */
    PageResult<WorkRecord> queryWorkRecordList(PageParams pageParams, QueryWorkRecordParamsDto queryWorkRecordParamsDto);

    /**
     * 根据作业ID获取当前用户的作业提交记录
     * @param workId 作业ID
     * @return 作业提交记录
     */
    WorkRecord getWorkRecordById(Long workId);

    /**
     * 根据作业记录ID获取评分信息
     * @param recordId 作业记录ID
     * @return 作业评分信息
     */
    WorkGrade getWorkGradeByRecordId(Long recordId);

    /**
     * 发布作业
     * @param workId 作业ID
     */
    void publishWork(Long workId);

    /**
     * 归档作业
     * @param workId 作业ID
     */
    void archiveWork(Long workId);

    /**
     * 查询作业列表
     * @param pageParams 分页参数
     * @param status 作业状态
     * @return 分页结果
     */
    PageResult<WorkInfo> queryWorkList(PageParams pageParams, String status);


    /**
     * 评分
     *
     * @param workGradeDTO 评分信息
     */
    void grade(WorkGradeDTO workGradeDTO);

    /**
     * 删除作业（仅草稿状态可删除）
     * @param workId 作业ID
     */
    void deleteWork(Long workId);

    /**
     * 下线作业（将发布状态的作业转为草稿状态）
     * @param workId 作业ID
     */
    void unpublishWork(Long workId);

    /**
     * 取消归档（将归档状态的作业转为发布状态）
     * @param workId 作业ID
     */
    void unarchiveWork(Long workId);

    /**
     * 获取作业批改详情
     * @param workId 作业ID
     * @return 作业批改详情
     */
    IWorkRecOverallDTO getWorkRecordReadOverAll(Long workId);
}